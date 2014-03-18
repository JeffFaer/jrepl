package falgout.jrepl.command.execute;

import java.io.IOException;

import falgout.jrepl.Environment;

public interface Executor<I> {
    public boolean execute(Environment env, I input) throws IOException;
}
