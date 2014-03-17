package falgout.jrepl.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import falgout.jrepl.Environment;

public class CompoundCommand<C extends Command> implements Command {
    private final List<? extends C> commands;
    
    public CompoundCommand(Collection<? extends C> commands) {
        this.commands = new ArrayList<>(commands);
    }
    
    @SafeVarargs
    public CompoundCommand(C... commands) {
        this(Arrays.asList(commands));
    }
    
    public List<? extends C> getCommands() {
        return Collections.unmodifiableList(commands);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commands == null) ? 0 : commands.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CompoundCommand<?> other = (CompoundCommand<?>) obj;
        if (commands == null) {
            if (other.commands != null) {
                return false;
            }
        } else if (!commands.equals(other.commands)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CompoundCommand [commands=");
        builder.append(commands);
        builder.append("]");
        return builder.toString();
    }
}
