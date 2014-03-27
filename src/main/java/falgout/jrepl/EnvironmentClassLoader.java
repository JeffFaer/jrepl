package falgout.jrepl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import falgout.jrepl.command.execute.codegen.GeneratedClass;
import falgout.jrepl.reflection.NestedClass;
import falgout.util.Closeables;

public class EnvironmentClassLoader extends URLClassLoader {
    private final Environment env;
    
    public EnvironmentClassLoader(Environment env) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        this.env = env;
        try {
            addURL(env.getGeneratedCodeLocation().toUri().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Generated code location should be accessible by URL.", e);
        }
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {}
        
        Optional<? extends NestedClass<?>> opt = env.getClassRepository().getCompiled(name);
        if (opt.isPresent()) {
            return opt.get().getDeclaredClass();
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
        Closeables.closeAll(super::close, env);
    }
}
