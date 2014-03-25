package falgout.jrepl.command.execute.codegen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedMethodTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    @Inject public CodeCompiler<Method> compiler;
    
    @Test
    public void blankMethodCanCompile() throws ExecutionException {
        GeneratedMethod g = new GeneratedMethod(e);
        compile(g);
    }
    
    private Method compile(GeneratedMethod g) throws ExecutionException {
        Method method = compiler.execute(e, g);
        assertEquals(g.getName(), method.getName());
        return method;
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws ExecutionException, ReflectiveOperationException {
        env.execute("final Object foo = 5;");
        
        GeneratedMethod g = new GeneratedMethod(e);
        g.addChild(new SourceCode<Statement>(null) {
            @Override
            public Statement getTarget(Class<?> clazz) throws ReflectiveOperationException {
                return mock(ReturnStatement.class);
            }
            
            @Override
            public String toString() {
                return "return foo;";
            }
        });
        compile(g);
    }
}
