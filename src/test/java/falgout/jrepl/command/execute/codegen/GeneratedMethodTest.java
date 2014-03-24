package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Expression;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.LocalVariable;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedMethodTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void blankMethodCanCompile() throws ExecutionException {
        GeneratedMethod g = new GeneratedMethod(e);
        compile(g);
    }
    
    private Method compile(GeneratedMethod g) throws ExecutionException {
        Method method = METHOD_COMPILER.execute(e, g);
        assertEquals(g.getName(), method.getName());
        return method;
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws ExecutionException, ReflectiveOperationException {
        LocalVariable<?> var = new LocalVariable<>(true, GoogleTypes.INT, "foo", 5);
        e.addVariable(var);
        
        GeneratedMethod g = new GeneratedMethod(e);
        g.addChild(getCode("return foo;"));
        compile(g);
    }
    
    private SourceCode<? extends WrappedStatement> getCode(String code) throws ReflectiveOperationException {
        SourceCode<WrappedStatement> sc = mock(SourceCode.class);
        WrappedStatement st = new WrappedStatement(mock(Expression.class));
        when(sc.getTarget(Matchers.any())).thenReturn(st);
        when(sc.toString()).thenReturn(code);
        
        return sc;
    }
}
