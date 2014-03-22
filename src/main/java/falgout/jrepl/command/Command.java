package falgout.jrepl.command;

import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

public interface Command<R> {
    public R execute(Environment env) throws ExecutionException;
}
