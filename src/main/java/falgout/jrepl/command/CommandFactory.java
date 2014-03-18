package falgout.jrepl.command;

import falgout.jrepl.Environment;

public interface CommandFactory {
    public Command getCommand(Environment env, String input);
}
