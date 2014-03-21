package falgout.jrepl;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvironmentClassLoader extends URLClassLoader {
    private final Set<Import> imports;
    private volatile Path dynamicCodeLocation;
    
    public EnvironmentClassLoader(Set<Import> imports) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.imports = imports;
    }
    
    public Path getDynamicCodeLocation() throws IOException {
        if (dynamicCodeLocation == null) {
            synchronized (this) {
                if (dynamicCodeLocation == null) {
                    dynamicCodeLocation = Files.createTempDirectory("Environment");
                    addURL(dynamicCodeLocation.toUri().toURL());
                }
            }
        }
        
        return dynamicCodeLocation;
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {}
        
        return loadImportedClass(name);
    }
    
    private Class<?> loadImportedClass(String simpleName) throws ClassNotFoundException {
        return verify(simpleName, imports.stream().map(i -> i.resolveClass(simpleName)).collect(Collectors.toList()));
    }
    
    private List<Class<?>> loadAll(Iterable<String> names) {
        List<Class<?>> classes = new ArrayList<>();
        for (String className : names) {
            try {
                classes.add(super.loadClass(className));
            } catch (ClassNotFoundException e) {}
        }
        
        return classes;
    }
    
    private Class<?> verify(String name, Iterable<String> names) throws ClassNotFoundException {
        List<Class<?>> classes = loadAll(names);
        
        if (classes.size() > 1) {
            throw new ClassNotFoundException("Ambiguous import. " + classes);
        } else if (classes.size() == 0) {
            throw new ClassNotFoundException("Could not find " + name + " in " + imports);
        }
        
        return classes.get(0);
    }
    
    @Override
    public void close() throws IOException {
        IOException ex = null;
        try {
            super.close();
        } catch (IOException e) {
            ex = e;
        }
        
        if (dynamicCodeLocation != null) {
            try {
                Files.walkFileTree(dynamicCodeLocation, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            } catch (IOException e) {
                if (ex == null) {
                    ex = e;
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        
        if (ex != null) {
            throw ex;
        }
    }
}
