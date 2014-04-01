package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import falgout.jrepl.Import;
import falgout.jrepl.jdt.ValuedThrowingASTVisitor;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;
import falgout.jrepl.reflection.NestedClass;
import falgout.jrepl.reflection.TypeIdentifier;

public abstract class TypeSourceCode extends NestedSourceCode<Class<?>, Member> {
    public static abstract class Builder<S extends TypeSourceCode, B extends Builder<S, B>> extends
            NestedSourceCode.Builder<Class<?>, Member, S, B> {
        private String _package;
        private List<Import> imports = new ArrayList<>();
        private TypeToken<?> superclass = GoogleTypes.OBJECT;
        private List<TypeToken<?>> superinterfaces = new ArrayList<>();
        
        protected Builder() {}
        
        protected Builder(String preferredName) {
            super(preferredName);
        }
        
        @Override
        public B initialize(S source) {
            return super.initialize(source)
                    .setPackage(source.getPackage())
                    .setImports(new ArrayList<>(source.getImports()))
                    .setSuperclass(source.getSuperclass())
                    .setSuperinterfaces(new ArrayList<>(source.getSuperinterfaces()));
        }
        
        public String getPackage() {
            return _package;
        }
        
        public B setPackage(String _package) {
            this._package = _package;
            return getBuilder();
        }
        
        public List<Import> getImports() {
            return imports;
        }
        
        public B addImports(Import... imports) {
            this.imports.addAll(requireNonNull(imports));
            return getBuilder();
        }
        
        public B addImports(Iterable<? extends Import> imports) {
            this.imports.addAll(Lists.newArrayList(imports));
            return getBuilder();
        }
        
        public B setImports(List<Import> imports) {
            this.imports = requireNonNull(imports);
            return getBuilder();
        }
        
        protected TypeToken<?> getSuperclass() {
            return superclass;
        }
        
        protected B setSuperclass(TypeToken<?> superclass) {
            this.superclass = Objects.requireNonNull(superclass,
                    "superclass cannot be null (Did you mean Object.class?)");
            return getBuilder();
        }
        
        public List<TypeToken<?>> getSuperinterfaces() {
            return superinterfaces;
        }
        
        public B addSuperinterfaces(TypeToken<?>... superinterfaces) {
            this.superinterfaces.addAll(requireNonNull(superinterfaces));
            return getBuilder();
        }
        
        public B setSuperinterfaces(List<TypeToken<?>> superinterfaces) {
            this.superinterfaces = requireNonNull(superinterfaces);
            return getBuilder();
        }
        
        @Override
        protected S build(int modifiers, String name, List<SourceCode<? extends Member>> children) {
            return build(modifiers, name, children, _package, new ArrayList<>(imports), superclass, new ArrayList<>(
                    superinterfaces));
        }
        
        protected abstract S build(int modifiers, String name, List<SourceCode<? extends Member>> children,
                String _package, List<Import> imports, TypeToken<?> superclass, List<TypeToken<?>> superinterfaces);
    }
    
    private final String _package;
    private final List<Import> imports;
    private final TypeToken<?> superclass;
    private final List<TypeToken<?>> superinterfaces;
    
    protected TypeSourceCode(int modifiers, String name, List<SourceCode<? extends Member>> children, String _package,
            List<Import> imports, TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
        super(modifiers, name, children);
        this._package = _package;
        this.imports = imports;
        this.superclass = superclass;
        this.superinterfaces = superinterfaces;
    }
    
    public String getPackage() {
        return _package;
    }
    
    public List<? extends Import> getImports() {
        return Collections.unmodifiableList(imports);
    }
    
    protected TypeToken<?> getSuperclass() {
        return superclass;
    }
    
    protected List<? extends TypeToken<?>> getSuperinterfaces() {
        return Collections.unmodifiableList(superinterfaces);
    }
    
    @Override
    public Class<?> getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return clazz;
    }
    
    public NamedSourceCode<NestedClass<?>> asNestedClass(boolean _static) {
        int mods = getModifiers();
        if (_static) {
            mods |= Modifier.STATIC;
        }
        return new NamedSourceCode<NestedClass<?>>(mods, getName()) {
            @Override
            public NestedClass<?> getTarget(Class<?> clazz) throws ReflectiveOperationException {
                for (Class<?> c : clazz.getClasses()) {
                    if (c.getSimpleName().equals(getName())) {
                        return new NestedClass<>(c);
                    }
                }
                
                throw new ClassNotFoundException(clazz + "$" + getName());
            }
            
            @Override
            public String toString() {
                return getClassBody(getModifiers());
            }
        };
    }
    
    protected abstract TypeIdentifier getTypeIdentifier();
    
    private Optional<String> getSuperinterfaceList() {
        return superinterfaces.size() == 0 ? Optional.empty() : Optional.of(superinterfaces.stream()
                .map(t -> t.toString())
                .collect(joining(", ")));
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (_package != null) {
            b.append("package ").append(_package).append(";\n\n");
        }
        
        imports.forEach(i -> b.append(i).append("\n"));
        if (imports.size() > 0) {
            b.append("\n");
        }
        
        b.append(getClassBody(getModifiers()));
        
        return b.toString();
    }
    
    private String getClassBody(int modifiers) {
        StringBuilder b = new StringBuilder();
        TypeIdentifier id = getTypeIdentifier();
        b.append(getModifierString(modifiers)).append(id).append(" ").append(getName()).append(" ");
        
        if (!getSuperclass().equals(GoogleTypes.OBJECT) && id.getClassExtender().isPresent()) {
            b.append(id.getClassExtender().get()).append(" ").append(getSuperclass()).append(" ");
        }
        getSuperinterfaceList().ifPresent(l -> b.append(id.getInterfaceExtender()).append(" ").append(l).append(" "));
        b.append("{\n");
        b.append(createChildrenString("\n"));
        b.append("}");
        
        return b.toString();
    }
    
    public static TypeSourceCode get(AbstractTypeDeclaration node) throws ClassNotFoundException {
        return new ValuedThrowingASTVisitor<TypeSourceCode, ClassNotFoundException>(ClassNotFoundException.class) {
            @SuppressWarnings("unchecked")
            @Override
            public TypeSourceCode visit(TypeDeclaration node) throws ClassNotFoundException {
                Builder<?, ?> b;
                if (node.isInterface()) {
                    b = InterfaceSourceCode.builder();
                } else {
                    b = ClassSourceCode.builder();
                    Type t = node.getSuperclassType();
                    if (t != null) {
                        b.setSuperclass(JDTTypes.getType(t));
                    }
                }
                
                initialize(node, b);
                addSuperinterfaces(node.superInterfaceTypes(), b);
                
                return b.build();
            }
            
            private <B extends Builder<? extends TypeSourceCode, ?>> B initialize(AbstractTypeDeclaration node,
                    B builder) {
                builder.setModifiers(node.getModifiers());
                builder.setName(node.getName().toString());
                builder.addChildren(getBody(node.bodyDeclarations()));
                return builder;
            }
            
            private List<SourceCode<? extends Member>> getBody(List<BodyDeclaration> body) {
                List<SourceCode<? extends Member>> ret = new ArrayList<>();
                DelegateSourceCode.Builder<Member> delegate = DelegateSourceCode.builder();
                for (BodyDeclaration decl : (List<BodyDeclaration>) node.bodyDeclarations()) {
                    ret.add(delegate.setDelegate(decl).build());
                }
                
                return ret;
            }
            
            private <B extends Builder<?, ?>> B addSuperinterfaces(List<Type> superinterfaces, B builder)
                throws ClassNotFoundException {
                for (Type t : superinterfaces) {
                    builder.addSuperinterfaces(JDTTypes.getType(t));
                }
                
                return builder;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public TypeSourceCode visit(EnumDeclaration node) throws ClassNotFoundException {
                EnumSourceCode.Builder b = EnumSourceCode.builder();
                
                List<EnumConstantDeclaration> constants = node.enumConstants();
                SourceCode<? extends Member> constantDeclaration = new DelegateSourceCode<Member>(constants) {
                    @Override
                    public String toString() {
                        return constants.stream().map(decl -> decl.toString()).collect(joining(", ", "", ";"));
                    }
                };
                b.addChildren(constantDeclaration);
                
                initialize(node, b);
                addSuperinterfaces(node.superInterfaceTypes(), b);
                
                return b.build();
            }
            
        }.visit(node);
    }
}
