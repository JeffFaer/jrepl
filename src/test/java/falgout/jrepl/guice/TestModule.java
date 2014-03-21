package falgout.jrepl.guice;

import java.io.CharArrayWriter;
import java.io.IOException;

import com.google.common.io.CharSource;
import com.google.inject.AbstractModule;

import falgout.jrepl.EnvironmentModule;
import falgout.jrepl.command.CommandFactory;
import falgout.jrepl.command.JavaCommandFactory;

public class TestModule extends AbstractModule {
    public TestModule() {}
    
    @Override
    protected void configure() {
        bind(CommandFactory.class).to(JavaCommandFactory.class);

        CharArrayWriter out = new CharArrayWriter();
        CharArrayWriter err = new CharArrayWriter();

        bind(CharArrayWriter.class).annotatedWith(Stdout.class).toInstance(out);
        bind(CharArrayWriter.class).annotatedWith(Stderr.class).toInstance(err);
        
        try {
            install(new EnvironmentModule(CharSource.empty().openStream(), out, err));
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
