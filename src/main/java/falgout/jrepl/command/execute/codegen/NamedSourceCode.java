package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NamedSourceCode<T> implements SourceCode<T> {
    public static abstract class Builder<T, S extends NamedSourceCode<T>, B extends Builder<T, S, B>> extends
            SourceCode.Builder<T, S, B> {
        private static final String GENERATED_NAME_TEMPLATE = "$%s%d";
        private static final AtomicInteger ID = new AtomicInteger(0);
        protected static final int DEFAULT_MODIFIERS = Modifier.PUBLIC;
        protected static final String DEFAULT_NAME = "Generated";
        
        private final String preferredName;
        private List<SourceCode<? extends Annotation>> annotations = new ArrayList<>();
        private int modifiers = DEFAULT_MODIFIERS;
        private String name;
        
        protected Builder() {
            this(DEFAULT_NAME);
        }
        
        protected Builder(String preferredName) {
            this.preferredName = preferredName;
        }
        
        @Override
        public B initialize(S source) {
            this.setModifiers(source.getModifiers())
                    .setName(source.getName())
                    .setAnnotations(new ArrayList<>(source.getAnnotations()));
            return getBuilder();
        }
        
        public List<SourceCode<? extends Annotation>> getAnnotations() {
            return annotations;
        }
        
        @SafeVarargs
        public final B addAnnotation(SourceCode<? extends Annotation>... annotations) {
            this.annotations.addAll(requireNonNull(annotations));
            return getBuilder();
        }
        
        public B setAnnotations(List<SourceCode<? extends Annotation>> annotations) {
            this.annotations = requireNonNull(annotations);
            return getBuilder();
        }
        
        public int getModifiers() {
            return modifiers;
        }
        
        public B addModifier(int modifier) {
            modifiers |= modifier;
            return getBuilder();
        }
        
        public B setModifiers(int modifiers) {
            this.modifiers = modifiers;
            return getBuilder();
        }
        
        public String getName() {
            return name;
        }
        
        public B setName(String name) {
            this.name = name;
            return getBuilder();
        }
        
        @Override
        public S build() {
            String actualName = name != null ? name : String.format(GENERATED_NAME_TEMPLATE, preferredName,
                    ID.incrementAndGet());
            return build(annotations, modifiers, actualName);
        }
        
        protected abstract S build(List<SourceCode<? extends Annotation>> annotations, int modifiers, String name);
    }
    
    private final List<? extends SourceCode<? extends Annotation>> annotations;
    private final int modifiers;
    private final String name;
    
    protected NamedSourceCode(List<? extends SourceCode<? extends Annotation>> annotations, int modifiers, String name) {
        this.annotations = annotations;
        this.modifiers = modifiers;
        this.name = name;
    }
    
    public List<? extends SourceCode<? extends Annotation>> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    protected String getModifierString() {
        return getModifierString(modifiers);
    }
    
    protected String getModifierString(int modifiers) {
        StringBuilder b = new StringBuilder();
        
        b.append(annotations.stream().map(SourceCode::toString).collect(joining(" ")));
        if (b.length() != 0) {
            b.append(" ");
        }
        b.append(Modifier.toString(modifiers));
        
        if (b.length() == 0) {
            return "";
        } else {
            return b.append(" ").toString();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public abstract T getTarget(Class<?> clazz) throws ReflectiveOperationException;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof NamedSourceCode)) {
            return false;
        }
        NamedSourceCode<?> other = (NamedSourceCode<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    @Override
    public abstract String toString();
}
