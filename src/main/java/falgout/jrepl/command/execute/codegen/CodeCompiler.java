package falgout.jrepl.command.execute.codegen;

import falgout.jrepl.command.execute.Executor;

public interface CodeCompiler<T> extends Executor<SourceCode<? extends T>, T> {}
