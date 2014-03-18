package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentTest {
    @Inject public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void localVariablesAreAccessible() throws IOException {
        e.execute("int x = 5;");
        env.assertNoErrors();
        
        TypeToken<?> type = TypeToken.of(int.class);
        Map<String, ?> vars = e.getVariables(type);
        assertTrue(vars.containsKey("x"));
        assertEquals(5, vars.get("x"));
        
        assertEquals(5, e.getVariable("x", type));
    }
    
    @Test
    public void javaLangIsImportedByDefault() {
        Set<Import> imports = env.getEnvironment().getImports();
        assertThat(imports, Matchers.contains(Import.create("import java.lang.*;").toArray(new Import[1])));
    }
}
