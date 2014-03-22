package falgout.jrepl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void javaLangIsImportedByDefault() throws IOException {
        Set<Import> imports = e.getImports();
        assertThat(imports, contains(Import.create(false, "java.lang", true)));
    }
    
    @Test
    public void canAddImports() throws IOException {
        Import i = Import.create(false, "java.util", true);
        e.getImports().add(i);
        assertThat(e.getImports(), hasItem(i));
    }

    @Test
    public void cannotHaveDuplicateVariables() {
        Variable<?> var1 = new Variable<>(GoogleTypes.OBJECT, "foo", new Object());
        Variable<?> var2 = new Variable<>(GoogleTypes.OBJECT, "foo", new Object());

        assertTrue(e.addVariable(var1));
        assertFalse(e.addVariable(var2));

        assertSame(var1.get(), e.getVariable("foo").get());
    }
    
    @Test
    public void EnvironmentClassLoaderIsThreadContextClassLoader() {
        assertSame(e.getImportClassLoader(), Thread.currentThread().getContextClassLoader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void CannotAddUninitializedFinalVariable() {
        Variable<?> uninitFinal = new Variable<>(true, GoogleTypes.OBJECT, "foo");
        e.addVariable(uninitFinal);
    }
}
