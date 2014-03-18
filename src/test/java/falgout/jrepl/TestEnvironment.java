package falgout.jrepl;

import static org.junit.Assert.assertEquals;

import java.io.CharArrayWriter;

import org.junit.rules.ExternalResource;

import com.google.inject.Inject;

import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

public class TestEnvironment extends ExternalResource {
    private final Environment e;
    private final CharArrayWriter out;
    private final CharArrayWriter err;
    
    @Inject
    public TestEnvironment(Environment e, @Stdout CharArrayWriter out, @Stderr CharArrayWriter err) {
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
        String err = this.err.toString();
        assertEquals(err, 0, err.length());
    }
    
    @Override
    protected void after() {
        out.reset();
        err.reset();
    }
}
