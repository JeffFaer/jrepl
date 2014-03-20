package falgout.jrepl.command;

import falgout.jrepl.Environment;

public interface CommandFactory<R> {
    public Command<? extends R> getCommand(Environment env, String input);
}
