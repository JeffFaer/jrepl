package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Test;

import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;

public class EnvironmentTest {
    public Environment e = new Environment();
    
    @Test
    public void localVariablesAreAccessible() throws IOException {
        assertNoErrors("int x = 5;");
        
        TypeToken<?> type = TypeToken.of(int.class);
        Map<String, ?> vars = e.get(type);
        assertTrue(vars.containsKey("x"));
        assertEquals(5, vars.get("x"));
        
        assertEquals(5, e.get("x", type));
    }
    
    private void assertNoErrors(String... inputs) throws IOException {
        StringWriter sink = new StringWriter();
        
        for (String input : inputs) {
            e.execute(input, CharStreams.nullWriter(), sink);
            
            assertEquals(0, sink.toString().length());
        }
    }
}
