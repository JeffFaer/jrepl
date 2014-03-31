package falgout.jrepl.command.execute.codegen;

import falgout.jrepl.command.execute.AbstractBatchExecutor;

public abstract class CodeExecutor<T, R> extends AbstractBatchExecutor<NamedSourceCode<? extends T>, R> {
    protected CodeExecutor() {}
}
