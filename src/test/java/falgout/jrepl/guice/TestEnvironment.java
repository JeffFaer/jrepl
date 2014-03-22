package falgout.jrepl.guice;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.CharArrayWriter;
import java.io.IOException;

import org.junit.rules.ExternalResource;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.command.CommandFactory;

public class TestEnvironment extends ExternalResource {
    private final CommandFactory<?> f;
    private final Environment e;
    private final CharArrayWriter out;
    private final CharArrayWriter err;

    @Inject
    public TestEnvironment(CommandFactory<?> f, Environment e, @Stdout CharArrayWriter out, @Stderr CharArrayWriter err) {
        this.f = f;
        this.e = e;
        this.out = out;
        this.err = err;
    }

    public CommandFactory<?> getFactory() {
        return f;
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

    public void execute(String input) throws IOException {
        f.execute(e, input);
    }

    public void executeNoErrors(String input) throws IOException {
        execute(input);
        assertNoErrors();
    }

    public void assertOutput(String expected) {
        assertEquals(expected, out.toString());
    }

    public void assertNoErrors() {
        String err = this.err.toString();
        if (!err.isEmpty()) {
            System.err.print(err);
            fail(err);
        }
    }
    
    public void assertErrors() {
        assertThat(err.toString(), not(isEmptyString()));
    }

    @Override
    protected void after() {
        out.reset();
        err.reset();

        try {
            e.close();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
