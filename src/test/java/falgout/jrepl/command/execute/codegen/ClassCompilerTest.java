package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.ClassCompiler.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class ClassCompilerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void CanCompileClass() throws ExecutionException, ReflectiveOperationException {
        Class<?> clazz = INSTANCE.execute(e, getCode("Foo", "public class Foo{}"));
        
        assertEquals("Foo", clazz.getName());
    }
    
    @Test(expected = ExecutionException.class)
    public void ProvidesErrorFeedbackIfCannotCompile() throws ExecutionException, ReflectiveOperationException {
        INSTANCE.execute(e, getCode("Foo", "public class Foo { ERROR }"));
    }
    
    private SourceCode<? extends Class<?>> getCode(String name, String code) throws ReflectiveOperationException {
        SourceCode<Class<?>> clazz = mock(SourceCode.class);
        when(clazz.getName()).thenReturn(name);
        when(clazz.toString()).thenReturn(code);
        when(clazz.getTarget(Matchers.any())).then(returnParameter());
        return clazz;
    }
    
    private Answer<?> returnParameter() {
        return invocation -> invocation.getArguments()[0];
    }
}
