package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

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
public class MemberCompilerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void automaticallyCompiledMethods() throws IOException {
        Optional<? extends Method> opt = METHOD_COMPILER.execute(e, getCode("foo", "public void foo() { }"));
        assertTrue(opt.isPresent());
        assertEquals("foo", opt.get().getName());
    }

    @Test
    public void ProvidesErrorFeedbackIfCannotCompile() throws IOException {
        Optional<? extends Method> opt = METHOD_COMPILER.execute(e, getCode("foo", "public void foo() { ERROR }"));
        assertFalse(opt.isPresent());
        assertThat(env.getError().toString(), not(isEmptyString()));
    }

    private SourceCode<? extends Method> getCode(String name, String code) {
        return new SourceCode<Method>(name) {
            @Override
            public Method getTarget(Class<?> clazz) {
                try {
                    return clazz.getMethod(name);
                } catch (NoSuchMethodException e) {
                    throw new Error(e);
                }
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