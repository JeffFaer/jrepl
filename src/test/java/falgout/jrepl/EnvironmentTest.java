package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.guice.Stderr;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentTest {
    @Inject public Environment e;
    @Inject @Stderr public StringWriter error;
    
    public void assertNoErrors() {
        assertEquals(0, error.toString().length());
    }
    
    @Test
    public void localVariablesAreAccessible() throws IOException {
        e.execute("int x = 5;");
        assertNoErrors();
        
        TypeToken<?> type = TypeToken.of(int.class);
        Map<String, ?> vars = e.getVariables(type);
        assertTrue(vars.containsKey("x"));
        assertEquals(5, vars.get("x"));
        
        assertEquals(5, e.getVariable("x", type));
    }
}
