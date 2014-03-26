package falgout.jrepl.jdt;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.reflection.GoogleTypes;

public class TypeResolver extends ValuedThrowingASTVisitor<TypeToken<?>, ClassNotFoundException> {
    private final ClassLoader cl;
    
    public TypeResolver(ClassLoader cl) {
        super(ClassNotFoundException.class);
        this.cl = cl;
    }
    
    private TypeToken<?> load(String name) throws ClassNotFoundException {
        return TypeToken.of(cl.loadClass(name));
    }
    
    // types
    
    @Override
    public TypeToken<?> visit(ArrayType node) throws ClassNotFoundException {
        return GoogleTypes.addArrays(visit(node.getElementType()), node.getDimensions());
    }
    
    @Override
    public TypeToken<?> visit(ParameterizedType node) throws ClassNotFoundException {
        Type raw = node.getType();
        TypeToken<?> owner = null;
        TypeToken<?> rawType = visit(raw);
        if (raw.isQualifiedType()) {
            owner = TypeToken.of(((java.lang.reflect.ParameterizedType) rawType.getType()).getOwnerType());
        }
        
        int numArgs = node.typeArguments().size();
        TypeToken<?>[] args = new TypeToken<?>[numArgs];
        for (int i = 0; i < args.length; i++) {
            args[i] = visit((Type) node.typeArguments().get(i));
        }
        
        int expected = rawType.getRawType().getTypeParameters().length;
        if (numArgs != expected) {
            String message = String.format("Incorrect number of type arguments for %s. Expected %d. Actual: %s",
                    rawType.getRawType().getSimpleName(), expected, Arrays.toString(args));
            throw new ClassNotFoundException(message);
        }
        
        return GoogleTypes.newParameterizedType(owner, rawType, args);
    }
    
    @Override
    public TypeToken<?> visit(PrimitiveType node) throws ClassNotFoundException {
        return GoogleTypes.getPrimitive(node.getPrimitiveTypeCode().toString());
    }
    
    @Override
    public TypeToken<?> visit(QualifiedType node) throws ClassNotFoundException {
        TypeToken<?> owner = visit(node.getQualifier());
        String name = owner.getRawType().getCanonicalName() + "$" + node.getName().getIdentifier();
        return GoogleTypes.newParameterizedType(owner, load(name));
    }
    
    @Override
    public TypeToken<?> visit(SimpleType node) throws ClassNotFoundException {
        return visit(node.getName());
    }
    
    public TypeToken<?> visit(Name node) throws ClassNotFoundException {
        return load(node.getFullyQualifiedName());
    }
    
    @Override
    public TypeToken<?> visit(SimpleName node) throws ClassNotFoundException {
        return visit((Name) node);
    }
    
    @Override
    public TypeToken<?> visit(QualifiedName node) throws ClassNotFoundException {
        return visit((Name) node);
    }
    
    @Override
    public TypeToken<?> visit(WildcardType node) throws ClassNotFoundException {
        TypeToken<?> t = visit(node.getBound());
        return node.isUpperBound() ? GoogleTypes.subtypeOf(t) : GoogleTypes.supertypeOf(t);
    }
}
