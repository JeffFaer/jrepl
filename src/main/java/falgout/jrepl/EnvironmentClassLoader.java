package falgout.jrepl;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import falgout.jrepl.command.execute.codegen.GeneratedClass;

public class EnvironmentClassLoader extends URLClassLoader {
    private final Environment env;
    private volatile Path dynamicCodeLocation;
    
    public EnvironmentClassLoader(Environment env) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.env = env;
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
        
        if (env.containsClass(name)) {
            return env.getClass(name).getDeclaredClass();
        }
        
        return loadImportedClass(name);
    }
    
    private Class<?> loadImportedClass(String simpleName) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            // check to see if we generated it
            clazz = super.loadClass(GeneratedClass.PACKAGE + "." + simpleName);
        } catch (ClassNotFoundException e) {}
        
        Set<String> names = env.getImports().stream().map(i -> i.resolveClass(simpleName)).collect(Collectors.toSet());
        for (String name : names) {
            Class<?> next;
            try {
                next = super.loadClass(name);
            } catch (ClassNotFoundException e) {
                continue;
            }
            
            if (clazz != null) {
                throw new ClassNotFoundException("Ambiguous import. " + Arrays.asList(next, clazz));
            } else {
                clazz = next;
            }
        }
        
        if (clazz == null) {
            throw new ClassNotFoundException("Could not find " + simpleName + " in " + env.getImports());
        }
        
        return clazz;
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
