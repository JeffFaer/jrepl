package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class NestedSourceCode<T, C> extends NamedSourceCode<T> {
    public static abstract class Builder<T, C, S extends NestedSourceCode<T, C>, B extends Builder<T, C, S, B>> extends
            NamedSourceCode.Builder<T, S, B> {
        private List<SourceCode<? extends C>> children = new ArrayList<>();
        
        protected Builder() {
            super();
        }
        
        protected Builder(String preferredName) {
            super(preferredName);
        }
        
        public List<SourceCode<? extends C>> getChildren() {
            return children;
        }
        
        @SafeVarargs
        public final B addChildren(SourceCode<? extends C>... children) {
            return addChildren(Arrays.asList(children));
        }
        
        public B addChildren(Iterable<? extends SourceCode<? extends C>> children) {
            requireNonNull(children).forEach(this.children::add);
            return getBuilder();
        }
        
        public B setChildren(List<SourceCode<? extends C>> children) {
            this.children = requireNonNull(children);
            return getBuilder();
        }
        
        @Override
        protected S build(int modifiers, String name) {
            return build(modifiers, name, new ArrayList<>(children));
        }
        
        protected abstract S build(int modifiers, String name, List<SourceCode<? extends C>> children);
    }
    
    protected static final String TAB = "    ";
    private final List<SourceCode<? extends C>> children;
    
    protected NestedSourceCode(int modifiers, String name, List<SourceCode<? extends C>> children) {
        super(modifiers, name);
        this.children = children;
    }
    
    public List<? extends SourceCode<? extends C>> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    private String tabify(Object o) {
        StringBuilder b = new StringBuilder();
        Stream.of(o.toString().split("\n")).forEach(line -> b.append(TAB).append(line).append("\n"));
        return b.toString();
    }
    
    protected String createChildrenString(String delim) {
        return children.stream().map(this::tabify).collect(joining(delim));
    }
}
