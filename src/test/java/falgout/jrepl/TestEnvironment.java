package falgout.jrepl;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import com.google.inject.Inject;

import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

public class TestEnvironment {
    private final Environment e;
    private final StringWriter out;
    private final StringWriter err;
    
    @Inject
    public TestEnvironment(Environment e, @Stdout StringWriter out, @Stderr StringWriter err) {
        this.e = e;
        this.out = out;
        this.err = err;
    }
    
    public Environment getEnvironment() {
        return e;
    }
    
    public void assertOutput(String expected) {
        assertEquals(expected, out.toString());
    }
    
    public void assertNoErrors() {
        assertEquals(0, err.toString().length());
    }
}
