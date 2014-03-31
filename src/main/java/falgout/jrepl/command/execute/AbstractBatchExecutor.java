package falgout.jrepl.command.execute;

public abstract class AbstractBatchExecutor<I, R> extends AbstractExecutor<I, R> implements BatchExecutor<I, R> {
    protected AbstractBatchExecutor() {}
}
