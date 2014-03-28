package falgout.jrepl.command.execute.codegen;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedMethodTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    @Inject public CodeCompiler<Method> compiler;
    
    @Test
    public void blankMethodCanCompile() throws ExecutionException {
        MethodSourceCode.Builder b = MethodSourceCode.builder();
        compile(b.build());
    }
    
    private Method compile(MethodSourceCode g) throws ExecutionException {
        Method method = compiler.execute(e, g);
        assertEquals(g.getName(), method.getName());
        return method;
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws ExecutionException, ReflectiveOperationException {
        env.execute("final Object foo = 5;");
        
        MethodSourceCode.Builder b = MethodSourceCode.builder();
        b.addModifier(Modifier.STATIC);
        b.setReturnType(GoogleTypes.OBJECT);
        b.addChildren(new DelegateSourceCode<>("return foo;"));
        assertEquals(5, compile(b.build()).invoke(null));
    }
}
