package falgout.jrepl.guice;

import java.lang.reflect.Method;

import falgout.jrepl.command.execute.codegen.CodeExecutor;

public interface MethodExecutorFactory {
    public CodeExecutor<Method, Object> create(Object... args);
}
