package falgout.jrepl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

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
    public void CloseDeletesCodeDirectory() throws IOException {
        assertTrue(Files.exists(e.getGeneratedCodeLocation()));
        e.close();
        assertFalse(Files.exists(e.getGeneratedCodeLocation()));
    }
    
    @Test
    public void VariablesAreMembers() throws ExecutionException {
        assertEquals(0, e.getMembers().size());
        env.execute("Object foo;");
        assertEquals(1, e.getMembers().size());
    }
    
    @Test
    public void ClassesAreMembers() throws ExecutionException {
        assertEquals(0, e.getMembers().size());
        env.execute("public class Foo { }");
        assertEquals(1, e.getMembers().size());
    }
}
