package falgout.jrepl.command;

import java.io.IOException;
import java.util.Optional;

import falgout.jrepl.Environment;

public interface CommandFactory<R> {
    public Optional<? extends Command<? extends R>> getCommand(Environment env, String input);

    default public Optional<? extends R> execute(Environment env, String input) throws IOException {
        Optional<? extends Command<? extends R>> c = getCommand(env, input);
        if (c.isPresent()) {
            return Optional.of(c.get().execute(env));
        } else {
            return Optional.empty();
        }
    }
}
