package falgout.jrepl.guice;

import falgout.jrepl.command.execute.codegen.MethodExecutor;

public interface MethodExecutorFactory {
    public MethodExecutor create(Object[] args);
}
