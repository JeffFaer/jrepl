package falgout.jrepl.reflection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.common.reflect.Types2;

import falgout.jrepl.antlr4.ParseTreeUtils;
import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.BasicTypeContext;
import falgout.jrepl.parser.JavaParser.ReferenceTypeContext;
import falgout.jrepl.parser.JavaParser.TypeArgumentContext;
import falgout.jrepl.parser.JavaParser.TypeArgumentsContext;
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
    
    public static TypeToken<?> getType(ClassLoader cl, TypeContext type) throws ClassNotFoundException {
        BasicTypeContext basic = type.basicType();
        if (basic != null) {
            return getType(basic);
        } else {
            return getType(cl, type.referenceType());
        }
    }
    
    public static TypeToken<?> getType(BasicTypeContext basic) {
        return TypeToken.of(PRIMITIVES.get(basic.getText()));
    }
    
    public static TypeToken<?> getType(ClassLoader cl, ReferenceTypeContext reference) throws ClassNotFoundException {
        int extraArrays = reference.L_BRACKET().size();
        TypeToken<?> baseType;
        
        BasicTypeContext basic = reference.basicType();
        if (basic != null) {
            baseType = getType(basic);
        } else {
            Map<TerminalNode, Optional<List<TypeArgumentContext>>> identifiers = getIdentifiers(reference);
            
            String rawClassName = ParseTreeUtils.joinText(identifiers.keySet(), ".");
            Class<?> rawClass = cl.loadClass(rawClassName);
            baseType = createParameterizedType(cl, rawClass, identifiers);
        }
        
        return Types2.addArraysToType(baseType, extraArrays);
    }
    
    private static Map<TerminalNode, Optional<List<TypeArgumentContext>>> getIdentifiers(ReferenceTypeContext reference) {
        Map<TerminalNode, Optional<List<TypeArgumentContext>>> identifiers = new LinkedHashMap<>();
        TerminalNode identifier = null;
        loop: for (ParseTree child : reference.children) {
            if (child instanceof TerminalNode) {
                TerminalNode t = (TerminalNode) child;
                TerminalNode temp = null;
                switch (t.getSymbol().getType()) {
                case JavaParser.Identifier:
                    temp = t;
                    //$FALL-THROUGH$
                case JavaParser.DOT:
                    if (identifier != null) {
                        identifiers.put(identifier, Optional.<List<TypeArgumentContext>> absent());
                        identifier = null;
                    }
                    identifier = temp;
                    break;
                case JavaParser.L_BRACKET:
                case JavaParser.R_BRACKET:
                    break loop;
                default:
                    throw new Error("Unexpected symbol " + JavaParser.tokenNames[t.getSymbol().getType()]);
                }
            } else if (child instanceof TypeArgumentsContext) {
                identifiers.put(identifier, Optional.of(((TypeArgumentsContext) child).typeArgument()));
                identifier = null;
            }
        }
        
        if (identifier != null) {
            identifiers.put(identifier, Optional.<List<TypeArgumentContext>> absent());
        }
        
        return identifiers;
    }
    
    private static TypeToken<?> createParameterizedType(ClassLoader cl, Class<?> rawClass,
            Map<TerminalNode, Optional<List<TypeArgumentContext>>> identifiers) throws ClassNotFoundException {
        if (!Optional.presentInstances(identifiers.values()).iterator().hasNext()) {
            // no TypeArguments, we don't need to parameterize
            return TypeToken.of(rawClass);
        }
        
        LinkedList<Class<?>> classes = new LinkedList<>();
        do {
            classes.addFirst(rawClass);
        } while ((rawClass = rawClass.getEnclosingClass()) != null);
        
        if (classes.size() != identifiers.size()) {
            throw new Error("huh");
        }
        
        Type last = null;
        Type current = null;
        Iterator<Class<?>> rawClasses = classes.iterator();
        Iterator<Optional<List<TypeArgumentContext>>> typeArguments = identifiers.values().iterator();
        
        do {
            last = current;
            
            rawClass = rawClasses.next();
            List<Type> args = createTypeArguments(cl, typeArguments.next());
            
            if (rawClass.getTypeParameters().length != args.size()) {
                String s = String.format(
                        "Invalid type arguments for %s.\nExpected %d arguments. Actual arguments: <%s>",
                        rawClass.getSimpleName(), rawClass.getTypeParameters().length, Joiner.on(", ").join(args));
                throw new ClassNotFoundException(s);
            }
            
            Type[] argArray = args.toArray(new Type[args.size()]);
            
            if (last == null) {
                current = Types2.newParameterizedType(rawClass, argArray);
            } else {
                current = Types2.newParameterizedType(last, rawClass, argArray);
            }
        } while (rawClasses.hasNext());
        
        return TypeToken.of(current);
    }
    
    private static List<Type> createTypeArguments(ClassLoader cl, Optional<List<TypeArgumentContext>> arguments)
            throws ClassNotFoundException {
        List<TypeArgumentContext> contexts = arguments.or(Collections.EMPTY_LIST);
        List<Type> args = new ArrayList<>(contexts.size());
        
        for (TypeArgumentContext ctx : contexts) {
            TypeToken<?> type = getType(cl, ctx.referenceType());
            if (ctx.QUES() == null) {
                args.add(type.getType());
            } else {
                // TODO wildcard
                args.add(null);
            }
        }
        return args;
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
