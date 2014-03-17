package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.CharSource;
import com.google.common.reflect.TypeToken;

public class EnvironmentTest {
    public Environment e;
    public StringWriter outputSink;
    public StringWriter errorSink;
    
    @Before
    public void before() throws IOException {
        outputSink = new StringWriter();
        errorSink = new StringWriter();
        
        e = new Environment(CharSource.empty().openBufferedStream(), outputSink, errorSink);
    }
    
    public void assertNoErrors() {
        assertEquals(0, errorSink.toString().length());
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
