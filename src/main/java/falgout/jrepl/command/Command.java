package falgout.jrepl.command;

import java.io.IOException;

import falgout.jrepl.Environment;

public interface Command<R> {
    /**
     * 
     * @param env The {@code Environment} to execute in
     * @return The result of the execution
     * @throws IOException If an {@code IOException} occurs during execution
     */
    public R execute(Environment env) throws IOException;
}
