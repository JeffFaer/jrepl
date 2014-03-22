package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedMethodTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void blankMethodCanCompile() throws IOException {
        GeneratedMethod g = new GeneratedMethod(e);
        compile(g);
    }
    
    private Method compile(GeneratedMethod g) throws IOException {
        Optional<? extends Method> opt = METHOD_COMPILER.execute(e, g);
        assertTrue(opt.isPresent());
        assertEquals(g.getName(), opt.get().getName());
        return opt.get();
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws IOException {
        Variable<?> var = new Variable<>(true, GoogleTypes.INT, "foo", 5);
        e.addVariable(var);

        GeneratedMethod g = new GeneratedMethod(e);
        g.addChild(getCode("return foo;"));
        compile(g);
    }
    
    private SourceCode<? extends WrappedStatement> getCode(String code) {
        SourceCode<WrappedStatement> sc = mock(SourceCode.class);
        WrappedStatement st = new WrappedStatement(mock(Expression.class));
        when(sc.getTarget(Matchers.any())).thenReturn(st);
        when(sc.toString()).thenReturn(code);

        return sc;
    }
}
