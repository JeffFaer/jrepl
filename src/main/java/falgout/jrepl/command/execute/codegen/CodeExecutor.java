package falgout.jrepl.command.execute.codegen;

import falgout.jrepl.command.execute.BatchExecutor;

public abstract class CodeExecutor<T, R> extends BatchExecutor<NamedSourceCode<? extends T>, R> {
    protected CodeExecutor() {}
}
