package falgout.jrepl.command;

import com.google.inject.ImplementedBy;

import falgout.jrepl.Environment;

@ImplementedBy(JavaCommandFactory.class)
public interface CommandFactory {
    public Command<?> getCommand(Environment env, String input);
}
