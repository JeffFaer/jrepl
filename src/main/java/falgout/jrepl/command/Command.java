package falgout.jrepl.command;

import falgout.jrepl.Environment;

public interface Command<R> {
    public R execute(Environment env);
}
