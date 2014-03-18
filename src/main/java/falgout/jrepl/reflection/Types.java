package falgout.jrepl.reflection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.gui.TreeViewer;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.common.reflect.Types2;

import falgout.jrepl.Environment;
import falgout.jrepl.antlr4.ParseTreeUtils;
import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.BasicTypeContext;
import falgout.jrepl.parser.JavaParser.ReferenceTypeContext;
import falgout.jrepl.parser.JavaParser.TypeContext;
import falgout.jrepl.parser.JavaParser.VariableModifierContext;

public class Types {
    private static final Map<String, Class<?>> PRIMITIVES;
    static {
        PRIMITIVES = ImmutableMap.<String, Class<?>> builder()
                .put("byte", byte.class)
                .put("short", short.class)
                .put("char", char.class)
                .put("int", int.class)
                .put("long", long.class)
                .put("float", float.class)
                .put("double", double.class)
                .put("boolean", boolean.class)
                .build();
    }
    
    public static TypeToken<?> getType(Environment e, TypeContext type) throws ClassNotFoundException {
        BasicTypeContext basic = type.basicType();
        if (basic != null) {
            return getType(basic);
        } else {
            return getType(e, type.referenceType());
        }
    }
    
    public static TypeToken<?> getType(BasicTypeContext basic) throws ClassNotFoundException {
        return TypeToken.of(PRIMITIVES.get(basic.getText()));
    }
    
    public static TypeToken<?> getType(Environment e, ReferenceTypeContext reference) throws ClassNotFoundException {
        int extraArrays = reference.L_BRACKET().size();
        TypeToken<?> baseType;
        
        BasicTypeContext basic = reference.basicType();
        if (basic != null) {
            baseType = getType(basic);
        } else {
            String rawClassName = ParseTreeUtils.joinText(reference.Identifier(), ".");
            Class<?> rawClass = e.getImportClassLoader().loadClass(rawClassName);
            baseType = TypeToken.of(rawClass);
            // TODO TypeParams
            // baseType.where(typeParam, typeArg)
            
            new TreeViewer(Arrays.asList(JavaParser.ruleNames), reference).open();
        }
        
        return Types2.addArraysToType(baseType, extraArrays);
    }
    
    public static boolean isFinal(List<VariableModifierContext> modifiers) throws ModifierException {
        boolean isFinal = false;
        for (VariableModifierContext ctx : modifiers) {
            boolean fin = ctx.FINAL() != null;
            if (fin && !isFinal) {
                isFinal = true;
            } else if (fin && isFinal) {
                throw new ModifierException("Duplicate modifier final.");
            }
        }
        
        return isFinal;
    }
}
