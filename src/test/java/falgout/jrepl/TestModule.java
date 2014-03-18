package falgout.jrepl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.inject.AbstractModule;

import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            bind(Reader.class).toInstance(CharSource.empty().openStream());
            bind(Writer.class).annotatedWith(Stdout.class).toInstance(CharStreams.nullWriter());
            bind(Writer.class).annotatedWith(Stderr.class).toInstance(new StringWriter());
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
