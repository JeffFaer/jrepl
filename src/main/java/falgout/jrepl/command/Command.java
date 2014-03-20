package falgout.jrepl.command;

import java.io.IOException;

import falgout.jrepl.Environment;

public interface Command<R> {
    public R execute(Environment env, String input) throws IOException;
}
