package falgout.jrepl.command.execute.codegen;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import falgout.jrepl.Environment;

public enum MethodCompiler implements CodeCompiler<Method> {
    INSTANCE;

    @Override
    public Optional<? extends Method> execute(Environment env, SourceCode<? extends Method> input) throws IOException {
        GeneratedClass genClass = new GeneratedClass(env);
        genClass.addChild(input);
        Optional<? extends Class<?>> opt = ClassCompiler.INSTANCE.execute(env, genClass);
        if (opt.isPresent()) {
            return Optional.of(input.getTarget(opt.get()));
        } else {
            return Optional.empty();
        }
    }
}
