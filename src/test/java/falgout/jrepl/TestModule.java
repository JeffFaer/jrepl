package falgout.jrepl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.common.io.CharSource;
import com.google.inject.AbstractModule;
import com.google.inject.Key;

import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            bind(Reader.class).toInstance(CharSource.empty().openStream());
            
            StringWriter out = new StringWriter();
            StringWriter err = new StringWriter();
            Key<StringWriter> stdout = Key.get(StringWriter.class, Stdout.class);
            Key<StringWriter> stderr = Key.get(StringWriter.class, Stderr.class);
            bind(stdout).toInstance(out);
            bind(stderr).toInstance(err);
            bind(Writer.class).annotatedWith(Stdout.class).to(stdout);
            bind(Writer.class).annotatedWith(Stderr.class).to(stderr);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
