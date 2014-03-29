package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;
import falgout.jrepl.util.ThrowingFunction;

public abstract class MethodOrConstructorSourceCode<T> extends NestedSourceCode<T, Statement> {
    public static abstract class Builder<T, S extends MethodOrConstructorSourceCode<T>, B extends Builder<T, S, B>>
            extends NestedSourceCode.Builder<T, Statement, S, B> {
        private TypeToken<?> returnType = GoogleTypes.VOID;
        
        private List<TypeToken<?>> parameters = new ArrayList<>();
        private List<String> names = new ArrayList<>();
        private BiFunction<? super TypeToken<?>, ? super Integer, ? extends String> parameterNamer = (type, index) -> {
            String name = index >= names.size() ? null : names.get(index);
            return name == null ? "$param" + index : name;
        };
        
        private List<TypeToken<? extends Throwable>> thro = new ArrayList<>();
        
        protected Builder() {}
        
        protected Builder(String preferredName) {
            super(preferredName);
        }
        
        @Override
        public B initialize(S source) {
            names = new ArrayList<>(source.getParameterNames());
            return super.initialize(source)
                    .setParameters(new ArrayList<>(source.getParameters()))
                    .setReturnType(source.getReturnType())
                    .setThrows(new ArrayList<>(source.getThrownExceptions()));
        }
        
        protected TypeToken<?> getReturnType() {
            return returnType;
        }
        
        protected B setReturnType(TypeToken<?> returnType) {
            this.returnType = Objects.requireNonNull(returnType,
                    "returnType cannot be null. (Did you mean void.class?)");
            return getBuilder();
        }
        
        public List<TypeToken<?>> getParameters() {
            return parameters;
        }
        
        public B addParameters(TypeToken<?>... parameters) {
            this.parameters.addAll(requireNonNull(parameters));
            return getBuilder();
        }
        
        public B addParameterNames(String... names) {
            this.names.addAll(requireNonNull(names));
            return getBuilder();
        }
        
        public B setParameters(List<TypeToken<?>> parameters) {
            this.parameters = requireNonNull(parameters);
            return getBuilder();
        }
        
        public List<TypeToken<? extends Throwable>> getThrows() {
            return thro;
        }
        
        @SafeVarargs
        public final B addThrows(TypeToken<? extends Throwable>... thro) {
            return addThrows(Arrays.asList(thro));
        }
        
        public B addThrows(List<TypeToken<? extends Throwable>> thro) {
            this.thro.addAll(requireNonNull(thro));
            return getBuilder();
        }
        
        public B setThrows(List<TypeToken<? extends Throwable>> thro) {
            this.thro = requireNonNull(thro);
            return getBuilder();
        }
        
        @Override
        protected S build(int modifiers, String name, List<SourceCode<? extends Statement>> children) {
            List<String> actualNames = IntStream.range(0, parameters.size())
                    .mapToObj(i -> parameterNamer.apply(parameters.get(i), i))
                    .collect(Collectors.toList());
            
            return build(modifiers, name, children, returnType, new ArrayList<>(parameters), actualNames,
                    new ArrayList<>(thro));
        }
        
        protected abstract S build(int modifiers, String name, List<SourceCode<? extends Statement>> children,
                TypeToken<?> returnType, List<TypeToken<?>> parameters, List<String> parameterNames,
                List<TypeToken<? extends Throwable>> thro);
    }
    
    private final TypeToken<?> returnType;
    private final List<TypeToken<?>> parameters;
    private final List<String> parameterNames;
    private final List<TypeToken<? extends Throwable>> thro;
    
    protected MethodOrConstructorSourceCode(int modifiers, String name, List<SourceCode<? extends Statement>> children,
            TypeToken<?> returnType, List<TypeToken<?>> parameters, List<String> parameterNames,
            List<TypeToken<? extends Throwable>> thro) {
        super(modifiers, name, children);
        this.returnType = returnType;
        this.parameters = parameters;
        this.parameterNames = parameterNames;
        this.thro = thro;
    }
    
    protected TypeToken<?> getReturnType() {
        return returnType;
    }
    
    public List<? extends TypeToken<?>> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    
    public List<? extends String> getParameterNames() {
        return Collections.unmodifiableList(parameterNames);
    }
    
    public List<? extends TypeToken<? extends Throwable>> getThrownExceptions() {
        return Collections.unmodifiableList(thro);
    }
    
    protected Class<?>[] getRawTypes() {
        Class<?>[] raw = new Class<?>[parameters.size()];
        IntStream.range(0, raw.length).forEach(i -> raw[i] = parameters.get(i).getRawType());
        return raw;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(getRawTypes());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MethodOrConstructorSourceCode)) {
            return false;
        }
        MethodOrConstructorSourceCode<?> other = (MethodOrConstructorSourceCode<?>) obj;
        if (!Arrays.equals(getRawTypes(), other.getRawTypes())) {
            return false;
        }
        return true;
    }
    
    protected StringBuilder createDeclarationStub() {
        StringBuilder b = new StringBuilder();
        b.append(getModifierString());
        if (returnType != null) {
            b.append(GoogleTypes.toCanonicalString(returnType)).append(" ");
        }
        b.append(getName()).append("(");
        b.append(IntStream.range(0, parameters.size())
                .mapToObj(
                        i -> Arrays.asList(GoogleTypes.toCanonicalString(parameters.get(i)), " ", parameterNames.get(i)))
                .map(l -> String.join(" ", l))
                .collect(joining(", ")));
        b.append(") ");
        if (thro.size() > 0) {
            b.append("throws ");
            b.append(thro.stream().map(GoogleTypes::toCanonicalString).collect(joining(", ")));
        }
        
        return b;
    }
    
    @Override
    public String toString() {
        StringBuilder b = createDeclarationStub();
        if (Modifier.isAbstract(getModifiers())) {
            b.append(";");
        } else {
            b.append(" {\n");
            b.append(createChildrenString(""));
            b.append("}");
        }
        return b.toString();
    }
    
    protected static <T, S extends MethodOrConstructorSourceCode<T>, B extends Builder<T, S, B>> S initializeFrom(
            B builder, MethodDeclaration decl) throws ClassNotFoundException {
        builder.setModifiers(decl.getModifiers());
        builder.setName(decl.getName().toString());
        builder.setParameters(getParameterTypes(decl.parameters()));
        builder.setThrows(getExceptionTypes(decl.thrownExceptions()));
        
        List<Statement> l = decl.getBody().statements();
        DelegateSourceCode.Builder<Statement> b = DelegateSourceCode.builder();
        builder.addChildren(l.stream().map(st -> b.setDelegate(st).build()).collect(toList()));
        
        return builder.build();
    }
    
    private static List<TypeToken<?>> getParameterTypes(List<SingleVariableDeclaration> parameters)
        throws ClassNotFoundException {
        return getTypes(parameters, decl -> JDTTypes.getType(decl.getType()));
    }
    
    @SuppressWarnings("unchecked")
    private static List<TypeToken<? extends Throwable>> getExceptionTypes(List<Name> exceptions)
        throws ClassNotFoundException {
        List<? extends TypeToken<?>> types = getTypes(exceptions, JDTTypes::getType);
        for (TypeToken<?> type : types) {
            if (!GoogleTypes.THROWABLE.isAssignableFrom(type.getRawType())) {
                throw new IllegalArgumentException("Cannot throw a non-Throwable type");
            }
        }
        
        return (List<TypeToken<? extends Throwable>>) types;
    }
    
    private static <A extends ASTNode> List<TypeToken<?>> getTypes(List<A> types,
            ThrowingFunction<A, TypeToken<?>, ClassNotFoundException> resolver) throws ClassNotFoundException {
        List<TypeToken<?>> ret = new ArrayList<>(types.size());
        for (A decl : types) {
            ret.add(resolver.apply(decl));
        }
        
        return ret;
    }
}
