package falgout.jrepl.reflection;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

/**
 * Utility methods for manipulating Eclipse JDT's types.
 *
 * @author jeffrey
 */
public class JDTTypes {
    private static final Map<String, Class<?>> PRIMITIVES;
    static {
        Map<String, Class<?>> temp = new LinkedHashMap<>();
        for (Class<?> primitive : Primitives.allPrimitiveTypes()) {
            temp.put(primitive.getSimpleName(), primitive);
        }
        
        PRIMITIVES = ImmutableMap.<String, Class<?>> builder().putAll(temp).build();
    }
    
    public static TypeToken<?> getType(Type type) throws ClassNotFoundException {
        if (type.isArrayType()) {
            return getType((ArrayType) type);
        } else if (type.isParameterizedType()) {
            return getType((ParameterizedType) type);
        } else if (type.isPrimitiveType()) {
            return getType((PrimitiveType) type);
        } else if (type.isQualifiedType()) {
            return getType((QualifiedType) type);
        } else if (type.isSimpleType()) {
            return getType((SimpleType) type);
        } else if (type.isWildcardType()) {
            return getType((WildcardType) type);
        } else {
            throw new AssertionError();
        }
    }
    
    private static TypeToken<?> getType(ArrayType type) throws ClassNotFoundException {
        return GoogleTypes.addArrays(getType(type.getElementType()), type.getDimensions());
    }

    private static TypeToken<?> getType(ParameterizedType type) throws ClassNotFoundException {
        Type raw = type.getType();
        TypeToken<?> owner = null;
        TypeToken<?> rawType = getType(raw);
        if (raw.isQualifiedType()) {
            owner = TypeToken.of(((java.lang.reflect.ParameterizedType) rawType.getType()).getOwnerType());
        }
        
        int numArgs = type.typeArguments().size();
        TypeToken<?>[] args = new TypeToken<?>[numArgs];
        for (int i = 0; i < args.length; i++) {
            args[i] = getType((Type) type.typeArguments().get(i));
        }

        int expected = rawType.getRawType().getTypeParameters().length;
        if (numArgs != expected) {
            String message = String.format("Incorrect number of type arguments for %s. Expected %d. Actual: %s",
                    rawType.getRawType().getSimpleName(), expected, Arrays.toString(args));
            throw new ClassNotFoundException(message);
        }
        
        return GoogleTypes.newParameterizedType(owner, rawType, args);
    }

    private static TypeToken<?> getType(PrimitiveType type) {
        return TypeToken.of(PRIMITIVES.get(type.getPrimitiveTypeCode().toString()));
    }
    
    private static TypeToken<?> getType(QualifiedType type) throws ClassNotFoundException {
        TypeToken<?> owner = getType(type.getQualifier());
        String name = owner.getRawType().getCanonicalName() + "." + type.getName().getIdentifier();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> rawType = cl.loadClass(name);
        return GoogleTypes.newParameterizedType(owner, TypeToken.of(rawType));
    }
    
    private static TypeToken<?> getType(SimpleType type) throws ClassNotFoundException {
        String name = type.getName().getFullyQualifiedName();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return TypeToken.of(cl.loadClass(name));
    }
    
    private static TypeToken<?> getType(WildcardType type) throws ClassNotFoundException {
        TypeToken<?> t = getType(type.getBound());
        return type.isUpperBound() ? GoogleTypes.subtypeOf(t) : GoogleTypes.supertypeOf(t);
    }

    public static boolean isFinal(List<Modifier> modifiers) {
        return modifiers.stream().anyMatch(Modifier::isFinal);
    }
    
    public static boolean isStatic(List<Modifier> modifiers) {
        return modifiers.stream().anyMatch(Modifier::isStatic);
    }
    
    public static javax.lang.model.element.Modifier getVisibilityModifier(List<Modifier> modifiers) {
        for (Modifier mod : modifiers) {
            if (mod.isPublic()) {
                return javax.lang.model.element.Modifier.PUBLIC;
            } else if (mod.isPrivate()) {
                return javax.lang.model.element.Modifier.PRIVATE;
            } else if (mod.isProtected()) {
                return javax.lang.model.element.Modifier.PROTECTED;
            }
        }
        
        return javax.lang.model.element.Modifier.DEFAULT;
    }
}
