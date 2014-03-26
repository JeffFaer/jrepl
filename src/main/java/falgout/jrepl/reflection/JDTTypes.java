package falgout.jrepl.reflection;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.jdt.ExpressionResolver;
import falgout.jrepl.jdt.TypeResolver;

/**
 * Utility methods for manipulating Eclipse JDT's types.
 *
 * @author jeffrey
 */
public class JDTTypes {
    public static TypeResolver getTypeResolver() {
        return new TypeResolver(Thread.currentThread().getContextClassLoader());
    }
    
    public static TypeToken<?> getType(Name name) throws ClassNotFoundException {
        return getTypeResolver().visit(name);
    }
    
    public static TypeToken<?> getType(Type type) throws ClassNotFoundException {
        return getTypeResolver().visit(type);
    }
    
    public static TypeToken<?> getType(Expression expression, Environment env) throws ReflectiveOperationException {
        return new ExpressionResolver(env).visit(expression);
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
