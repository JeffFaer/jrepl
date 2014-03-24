package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import falgout.jrepl.Environment;

public abstract class GeneratedSourceCode<T, C> extends SourceCode<T> {
    private static final AtomicInteger ID = new AtomicInteger(0);
    public static final String TEMPLATE = "$Generated";
    public static final String TAB = "    ";
    
    private final Environment env;
    private final List<SourceCode<? extends C>> children = new ArrayList<>();
    
    public GeneratedSourceCode(Environment env) {
        super(TEMPLATE + ID.incrementAndGet());
        this.env = env;
    }
    
    public Environment getEnvironment() {
        return env;
    }
    
    public List<? extends SourceCode<? extends C>> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public boolean addChild(SourceCode<? extends C> child) {
        return children.add(child);
    }
    
    public boolean removeChild(SourceCode<? extends C> child) {
        return children.remove(child);
    }
    
    public String addTabsToChildren() {
        return addTabsToChildren("\n", "\n", "");
    }
    
    public String addTabsToChildren(String delim, String prefix, String suffix) {
        return children.stream().map(child -> child.toString()).map(s -> {
            StringBuilder b = new StringBuilder();
            for (String line : s.split("\n")) {
                b.append(TAB).append(line).append("\n");
            }
            return b.toString();
        }).collect(joining(delim, prefix, suffix));
    }
}
