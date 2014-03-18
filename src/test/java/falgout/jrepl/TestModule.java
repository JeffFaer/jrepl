package falgout.jrepl;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
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
            
            CharArrayWriter out = new CharArrayWriter();
            CharArrayWriter err = new CharArrayWriter();
            Key<CharArrayWriter> stdout = Key.get(CharArrayWriter.class, Stdout.class);
            Key<CharArrayWriter> stderr = Key.get(CharArrayWriter.class, Stderr.class);
            bind(stdout).toInstance(out);
            bind(stderr).toInstance(err);
            bind(Writer.class).annotatedWith(Stdout.class).to(stdout);
            bind(Writer.class).annotatedWith(Stderr.class).to(stderr);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
