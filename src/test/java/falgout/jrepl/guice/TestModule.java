package falgout.jrepl.guice;

import java.io.CharArrayWriter;
import java.io.IOException;

import com.google.common.io.CharSource;
import com.google.inject.AbstractModule;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentModule;
import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.execute.codegen.CodeGenModule;

public class TestModule extends AbstractModule {
    public TestModule() {}
    
    @Override
    protected void configure() {
        CharArrayWriter out = new CharArrayWriter();
        CharArrayWriter err = new CharArrayWriter();
        
        bind(CharArrayWriter.class).annotatedWith(Stdout.class).toInstance(out);
        bind(CharArrayWriter.class).annotatedWith(Stderr.class).toInstance(err);
        
        bind(Environment.class).toProvider(EnvironmentProvider.class);
        
        try {
            install(new EnvironmentModule(CharSource.empty().openStream(), out, err));
            install(new CommandModule());
            install(new CodeGenModule());
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
