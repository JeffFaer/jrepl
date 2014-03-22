package falgout.jrepl;

import static org.junit.Assert.assertSame;

import java.awt.Window.Type;
import java.io.IOException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.execute.codegen.ClassCompiler;
import falgout.jrepl.command.execute.codegen.GeneratedClass;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules({ TestModule.class, CommandModule.class })
public class EnvironmentClassLoaderTest {
    @Inject @Rule public TestEnvironment env;
    public ClassLoader cl;
    
    @Before
    public void before() {
        cl = env.getEnvironment().getImportClassLoader();
    }
    
    @Test
    public void LoadsImports() throws ClassNotFoundException {
        assertSame(Object.class, cl.loadClass("Object"));
    }

    @Test
    public void CanLoadNestedTypes() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.awt.Window.Type;");
        assertSame(Type.class, cl.loadClass("Type"));
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void CannotLoadAmbiguousClass() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.lang.reflect.Type; import java.awt.Window.Type;");
        cl.loadClass("Type");
    }
    
    @Test
    public void CanLoadGeneratedClasses() throws IOException, ClassNotFoundException {
        GeneratedClass c = new GeneratedClass(env.getEnvironment());
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env.getEnvironment(), c).get();

        assertSame(clazz, cl.loadClass(c.getName()));
    }
}
