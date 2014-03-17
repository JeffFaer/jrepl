package falgout.jrepl.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import falgout.jrepl.Environment;

public class CompoundCommand implements Command {
    private final List<Command> commands;
    
    public CompoundCommand(Collection<? extends Command> commands) {
        this.commands = new ArrayList<>(commands);
    }
    
    public CompoundCommand(Command... commands) {
        this(Arrays.asList(commands));
    }
    
    @Override
    public boolean execute(Environment e) throws IOException {
        for (Command c : commands) {
            if (!c.execute(e)) {
                return false;
            }
        }
        
        return true;
    }
}
