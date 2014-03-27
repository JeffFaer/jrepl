package falgout.jrepl.jdt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;
import falgout.utils.reflection.MethodInvoker;
import falgout.utils.reflection.MethodLocator;

public class ExpressionResolver extends ValuedThrowingASTVisitor<TypeToken<?>, ReflectiveOperationException> {
    private static final MethodLocator methodLocator = MethodInvoker.getDefault().getMethodLocator();
    private final Environment env;
    
    public ExpressionResolver(Environment env) {
        super(ReflectiveOperationException.class);
        this.env = env;
    }
    
    @Override
    public TypeToken<?> visit(ArrayAccess node) throws ReflectiveOperationException {
        return visit(node.getArray()).getComponentType();
    }
    
    @Override
    public TypeToken<?> visit(ArrayCreation node) throws ReflectiveOperationException {
        return visit(node.getType());
    }
    
    @Override
    public TypeToken<?> visit(Assignment node) throws ReflectiveOperationException {
        return visit(node.getRightHandSide());
    }
    
    @Override
    public TypeToken<Boolean> visit(BooleanLiteral node) {
        return GoogleTypes.BOOLEAN;
    }
    
    @Override
    public TypeToken<?> visit(CastExpression node) throws ReflectiveOperationException {
        return visit(node.getType());
    }
    
    @Override
    public TypeToken<?> visit(CharacterLiteral node) {
        return GoogleTypes.CHAR;
    }
    
    @Override
    public TypeToken<?> visit(ClassInstanceCreation node) throws ReflectiveOperationException {
        return visit(node.getType());
    }
    
    @Override
    public TypeToken<?> visit(ConditionalExpression node) throws ReflectiveOperationException {
        TypeToken<?> l = visit(node.getThenExpression());
        TypeToken<?> r = visit(node.getElseExpression());
        return l.isAssignableFrom(r) ? l : r;
    }
    
    @Override
    public TypeToken<?> visit(FieldAccess node) throws ReflectiveOperationException {
        Expression left = node.getExpression();
        SimpleName name = node.getName();
        if (left instanceof Name) {
            return resolveFieldOrQualifiedName((Name) left, name);
        } else {
            return resolveField(visit(node.getExpression()), name);
        }
    }
    
    private TypeToken<?> resolveFieldOrQualifiedName(Name left, SimpleName right) throws ReflectiveOperationException {
        ClassNotFoundException ex;
        try {
            TypeToken<?> test = JDTTypes.getType(left);
            return resolveField(test, right);
        } catch (ClassNotFoundException e) {
            ex = e;
        }
        
        AST ast = left.getAST();
        QualifiedName name = ast.newQualifiedName(left, right);
        try {
            return JDTTypes.getType(name);
        } catch (ClassNotFoundException e) {
            ex.addSuppressed(e);
            
            throw ex;
        }
    }
    
    private TypeToken<?> resolveField(TypeToken<?> type, SimpleName name) throws NoSuchFieldException {
        Field f = type.getRawType().getField(name.toString());
        return TypeToken.of(f.getGenericType());
    }
    
    @Override
    public TypeToken<?> visit(InfixExpression node) throws ReflectiveOperationException {
        // TODO Auto-generated method stub
        throw new Error("TODO");
    }
    
    @Override
    public TypeToken<?> visit(InstanceofExpression node) throws ReflectiveOperationException {
        return GoogleTypes.BOOLEAN;
    }
    
    @Override
    public TypeToken<?> visit(MethodInvocation node) throws ReflectiveOperationException {
        String name = node.getName().toString();
        Method method;
        if (node.getExpression() == null) {
            Optional<? extends Method> opt = env.getMethodRepository().getCompiled(name);
            if (opt.isPresent()) {
                method = opt.get();
            } else {
                throw new NoSuchMethodException(name + " does not exist in the Environment.");
            }
        } else {
            TypeToken<?> type = visit(node.getExpression());
            
            List<Expression> arguments = node.arguments();
            Class<?>[] args = new Class<?>[arguments.size()];
            for (int i = 0; i < arguments.size(); i++) {
                args[i] = visit(arguments.get(i)).getRawType();
            }
            
            method = methodLocator.getMethod(type.getRawType(), name, args);
        }
        return TypeToken.of(method.getGenericReturnType());
    }
    
    @Override
    public TypeToken<?> visit(SimpleName node) throws ReflectiveOperationException {
        String name = node.toString();
        Optional<? extends Variable<?>> var = env.getVariable(name);
        if (var.isPresent()) {
            return var.get().getType();
        } else {
            return JDTTypes.getType(node);
        }
    }
    
    @Override
    public TypeToken<?> visit(QualifiedName node) throws ReflectiveOperationException {
        return resolveFieldOrQualifiedName(node.getQualifier(), node.getName());
    }
    
    @Override
    public TypeToken<?> visit(NullLiteral node) {
        return GoogleTypes.OBJECT;
    }
    
    @Override
    public TypeToken<? extends Number> visit(NumberLiteral node) {
        String source = node.getToken();
        if (source.contains(".")) {
            if (source.contains("f") || source.contains("F")) {
                return GoogleTypes.FLOAT;
            } else {
                return GoogleTypes.DOUBLE;
            }
        } else {
            if (source.contains("l") || source.contains("L")) {
                return GoogleTypes.LONG;
            } else {
                return GoogleTypes.INT;
            }
        }
    }
    
    @Override
    public TypeToken<?> visit(ParenthesizedExpression node) throws ReflectiveOperationException {
        return visit(node.getExpression());
    }
    
    @Override
    public TypeToken<?> visit(PostfixExpression node) throws ReflectiveOperationException {
        return visit(node.getOperand());
    }
    
    @Override
    public TypeToken<?> visit(PrefixExpression node) throws ReflectiveOperationException {
        if (node.getOperator().equals(PrefixExpression.Operator.NOT)) {
            return GoogleTypes.BOOLEAN;
        }
        return visit(node.getOperand());
    }
    
    @Override
    public TypeToken<String> visit(StringLiteral node) {
        return GoogleTypes.STRING;
    }
    
    @Override
    public TypeToken<?> visit(SuperFieldAccess node) throws ReflectiveOperationException {
        // "super" won't compile in the repl, don't worry about it
        return GoogleTypes.VOID;
    }
    
    @Override
    public TypeToken<?> visit(SuperMethodInvocation node) throws ReflectiveOperationException {
        // "super" won't compile in the repl, don't worry about it
        return GoogleTypes.VOID;
    }
    
    @Override
    public TypeToken<?> visit(ThisExpression node) throws ReflectiveOperationException {
        // "this" won't compile in the repl, don't worry about it
        return GoogleTypes.VOID;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public TypeToken<Class<?>> visit(TypeLiteral node) throws ClassNotFoundException {
        TypeToken<?> type = JDTTypes.getType(node.getType());
        return (TypeToken<Class<?>>) GoogleTypes.newParameterizedType(null, TypeToken.of(Class.class), type);
    }
    
    @Override
    public TypeToken<?> visit(VariableDeclarationExpression node) throws ReflectiveOperationException {
        throw new Error("These should get picked up by LocalVariableDeclarer.");
    }
}
