package falgout.jrepl.reflection;

import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.reflect.TypeToken;

public class Types {
    
    public static TypeToken<?> getType(Type type) {
        // TODO
        return null;
    }

    public static boolean isFinal(List<Modifier> modifiers) {
        return modifiers.stream().anyMatch(Modifier::isFinal);
    }
}
