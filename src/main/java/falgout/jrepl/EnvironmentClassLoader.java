package falgout.jrepl;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.NESTED_CLASS_COMPILER;

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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.reflection.NestedClass;

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
        
        Class<?> clazz = loadEnvironmentClass(name);
        if (clazz != null) {
            return clazz;
        }
        
        return loadImportedClass(name);
    }
    
    private Class<?> loadEnvironmentClass(String name) {
        if (env.containsClass(name)) {
            SourceCode<NestedClass<?>> code = env.getClass(name);
            try {
                return NESTED_CLASS_COMPILER.execute(env, code).getDeclaredClass();
            } catch (ExecutionException e) {
                // if this class had already been compiled
                // this exception shouldn't be happening
                throw new Error(e);
            }
        }
        return null;
    }
    
    private Class<?> loadImportedClass(String simpleName) throws ClassNotFoundException {
        return verify(simpleName,
                env.getImports().stream().map(i -> i.resolveClass(simpleName)).collect(Collectors.toList()));
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
            throw new ClassNotFoundException("Could not find " + name + " in " + env.getImports());
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
