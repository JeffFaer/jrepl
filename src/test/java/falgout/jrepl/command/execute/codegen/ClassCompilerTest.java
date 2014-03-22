package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.ClassCompiler.INSTANCE;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

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
public class ClassCompilerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void CanCompileClass() throws ExecutionException {
        Class<?> clazz = INSTANCE.execute(e, getCode("Foo", "public class Foo{}"));
        
        assertEquals("Foo", clazz.getName());
    }
    
    @Test(expected = ExecutionException.class)
    public void ProvidesErrorFeedbackIfCannotCompile() throws ExecutionException {
        INSTANCE.execute(e, getCode("Foo", "public class Foo { ERROR }"));
    }

    private SourceCode<? extends Class<?>> getCode(String name, String code) {
        return new SourceCode<Class<?>>(name) {
            @Override
            public Class<?> getTarget(Class<?> clazz) {
                return clazz;
            }

            @Override
            public NestingKind getNestingKind() {
                return null;
            }

            @Override
            public Modifier getAccessLevel() {
                return null;
            }

            @Override
            public String toString() {
                return code;
            }
        };
    }
}
