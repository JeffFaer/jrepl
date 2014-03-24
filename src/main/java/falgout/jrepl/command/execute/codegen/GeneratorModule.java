package falgout.jrepl.command.execute.codegen;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;

/**
 * Binds each variable in an {@code Environment} to an {@link Named} variable of
 * the same type.
 *
 * @author jeffrey
 */
public class GeneratorModule extends AbstractModule {
    private final Environment env;
    private final Class<?>[] classes;
    
    public GeneratorModule(Environment env, Class<?>... generatedClasses) {
        this.env = env;
        classes = generatedClasses;
    }
    
    @Override
    protected void configure() {
        requestStaticInjection(classes);
        
        for (Variable<?> var : env.getVariables()) {
            bindVariable(var);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> void bindVariable(Variable<T> var) {
        if (var.isInitialized()) {
            Key<T> key = (Key<T>) Key.get(var.getType().getType(), Names.named(var.getName()));
            T value = var.get();
            if (value == null) {
                bind(key).toProvider(Providers.of(null));
            } else {
                bind(key).toInstance(value);
            }
        }
    }
}
