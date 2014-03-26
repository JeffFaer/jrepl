package falgout.jrepl.command.execute;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeExecutor;
import falgout.jrepl.command.execute.codegen.GeneratedMethod;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.guice.MethodExecutorFactory;

@Singleton
public class ExpressionExecutor extends BatchExecutor<Expression, Object> {
    private final CodeExecutor<Method, Object> executor;
    
    @Inject
    public ExpressionExecutor(MethodExecutorFactory factory) {
        executor = factory.create();
    }
    
    @Override
    public List<? extends Object> execute(Environment env, Iterable<? extends Expression> input)
            throws ExecutionException {
        List<GeneratedMethod> methods = new ArrayList<>();
        
        input.forEach(e -> {
            SourceCode<Statement> st;
            if (e instanceof MethodInvocation) {
                st = SourceCode.createStatement(e);
            } else {
                st = SourceCode.createReturnStatement(e);
            }
            
            GeneratedMethod method = new GeneratedMethod(env);
            method.addChild(st);
            methods.add(method);
        });
        
        return executor.execute(env, methods);
    }
}
