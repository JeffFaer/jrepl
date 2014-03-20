package falgout.jrepl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.google.inject.AbstractModule;

import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

public class EnvironmentModule extends AbstractModule {
    private final Reader stdin;
    private final Writer stdout;
    private final Writer stderr;
    
    public EnvironmentModule() {
        this(System.in, System.out, System.err);
    }
    
    public EnvironmentModule(InputStream in, OutputStream out, OutputStream err) {
        this(new InputStreamReader(in), new OutputStreamWriter(out), new OutputStreamWriter(err));
    }

    public EnvironmentModule(Reader stdin, Writer stdout, Writer stderr) {
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }
    
    @Override
    protected void configure() {
        bind(Reader.class).toInstance(stdin);
        bind(Writer.class).annotatedWith(Stdout.class).toInstance(stdout);
        bind(Writer.class).annotatedWith(Stderr.class).toInstance(stderr);
    }
}
