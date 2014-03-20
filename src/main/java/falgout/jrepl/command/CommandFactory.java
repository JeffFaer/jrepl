package falgout.jrepl.command;

import com.google.inject.ImplementedBy;

import falgout.jrepl.Environment;

@ImplementedBy(JavaCommandFactory.class)
public interface CommandFactory<R> {
    public Command<? extends R> getCommand(Environment env, String input);
}
