package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.reflection.Invokable;

public enum GeneratedMethodExecutor implements Executor<GeneratedMethod, Invokable.Method> {
    INSTANCE;

    /**
     * Creates an {@link Invokable} from the given {@code GeneratedMethod}. When
     * {@link Invokable#invoke invoked}, it will execute the method. The
     * receiver object is initialized with a {@link GeneratorModule}.
     */
    @Override
    public Optional<? extends Invokable.Method> execute(Environment env, GeneratedMethod input) throws IOException {
        Optional<? extends java.lang.reflect.Method> opt = METHOD_COMPILER.execute(env, input);
        if (opt.isPresent()) {
            java.lang.reflect.Method m = opt.get();
            Object receiver;
            if (Modifier.isStatic(m.getModifiers())) {
                receiver = null;
            } else {
                Class<?> clazz = m.getDeclaringClass();
                Injector i = Guice.createInjector(new GeneratorModule(env));
                receiver = i.getInstance(clazz);
            }

            return Optional.of(Invokable.from(receiver, m));
        } else {
            return Optional.empty();
        }
    }
}
