package falgout.jrepl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;

    @Test
    public void localVariablesAreAccessible() throws IOException {
        env.executeNoErrors("int x = 5;");

        TypeToken<?> type = TypeToken.of(int.class);
        assertTrue(e.containsVariable("x"));
        assertEquals(5, e.getVariable("x", type));
    }

    @Test
    public void javaLangIsImportedByDefault() throws IOException {
        Set<Import> imports = e.getImports();
        assertThat(imports, contains(ImportTest.create(env, "import java.lang.*;").toArray(new Import[1])));
    }

    @Test
    public void canAddImports() throws IOException {
        env.executeNoErrors("import java.util.List;");
        
        assertThat(e.getImports(), hasItem(ImportTest.create(env, "import java.util.List;").get(0)));
    }

    @Test
    public void parsingErrorsDontTakeUpExtraLines() throws IOException {
        env.getEnvironment().execute("int foo");
        String error = env.getError().toString();
        assertThat(error, endsWith("\n"));
        assertThat(error, not(endsWith("\n\n")));
    }
}
