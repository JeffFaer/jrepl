package falgout.jrepl;

import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.CharArrayWriter;
import java.io.IOException;

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
    
    public CharArrayWriter getOutput() {
        return out;
    }
    
    public CharArrayWriter getError() {
        return err;
    }
    
    public Environment getEnvironment() {
        return e;
    }
    
    public void executeNoErrors(String input) throws IOException {
        e.execute(input);
        assertNoErrors();
    }
    
    public void assertOutput(String expected) {
        assertEquals(expected, out.toString());
    }
    
    public void assertNoErrors() {
        String err = this.err.toString();
        assertThat(err, isEmptyString());
    }
    
    @Override
    protected void after() {
        out.reset();
        err.reset();
    }
}
