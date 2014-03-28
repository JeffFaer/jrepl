package falgout.jrepl.command.execute.codegen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.stubbing.Answer;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class MemberCompilerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    @Inject public CodeCompiler<Method> compiler;
    
    @Test
    public void automaticallyCompiledMethods() throws ExecutionException, ReflectiveOperationException {
        Method method = compiler.execute(e, getCode("foo", "public void foo() { }"));
        assertEquals("foo", method.getName());
    }
    
    @Test(expected = ExecutionException.class)
    public void ProvidesErrorFeedbackIfCannotCompile() throws ExecutionException, ReflectiveOperationException {
        compiler.execute(e, getCode("foo", "public void foo() { ERROR }"));
    }
    
    private NamedSourceCode<? extends Method> getCode(String name, String code) throws ReflectiveOperationException {
        NamedSourceCode<Method> method = mock(NamedSourceCode.class);
        when(method.getName()).thenReturn(name);
        when(method.toString()).thenReturn(code);
        when(method.getTarget(Matchers.any())).then(returnMethod(name));
        return method;
    }
    
    private Answer<?> returnMethod(String name) {
        return invocation -> ((Class<?>) invocation.getArguments()[0]).getMethod(name);
    }
}
