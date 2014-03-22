package falgout.jrepl.command;

import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

public interface CommandFactory<R> {
    public Command<? extends R> getCommand(Environment env, String input) throws ParsingException;
    
    default public R execute(Environment env, String input) throws ParsingException, ExecutionException {
        Command<? extends R> c = getCommand(env, input);
        return c.execute(env);
    }
}
