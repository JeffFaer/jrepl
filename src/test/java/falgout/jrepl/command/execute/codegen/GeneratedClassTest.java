package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.ClassCompiler.INSTANCE;
import static org.junit.Assert.assertEquals;

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

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedClassTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void blankClassCanCompile() throws ExecutionException {
        ClassSourceCode c = new ClassSourceCode.Builder().build();
        compile(c);
    }
    
    private Class<?> compile(ClassSourceCode clazz) throws ExecutionException {
        Class<?> c = INSTANCE.execute(e, clazz);
        assertEquals(clazz.getName(), c.getSimpleName());
        return c;
    }
}
