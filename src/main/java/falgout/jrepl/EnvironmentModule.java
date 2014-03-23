package falgout.jrepl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import falgout.jrepl.guice.CodeDirectory;
import falgout.jrepl.guice.EnvironmentProvider;
import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;
import falgout.jrepl.guice.TemporaryDirectoryProvider;

public class EnvironmentModule extends AbstractModule {
    private final Reader stdin;
    private final Writer stdout;
    private final Writer stderr;
    private final Provider<Path> dir;
    
    public EnvironmentModule() {
        this(System.in, System.out, System.err);
    }
    
    public EnvironmentModule(InputStream in, OutputStream out, OutputStream err) {
        this(in, out, err, getDefaultDirectory());
    }
    
    private static Provider<Path> getDefaultDirectory() {
        return new TemporaryDirectoryProvider("Environment");
    }
    
    public EnvironmentModule(InputStream in, OutputStream out, OutputStream err, Provider<Path> dir) {
        this(new InputStreamReader(in), new OutputStreamWriter(out), new OutputStreamWriter(err), dir);
    }
    
    public EnvironmentModule(Reader stdin, Writer stdout, Writer stderr) {
        this(stdin, stdout, stderr, getDefaultDirectory());
    }
    
    public EnvironmentModule(Reader stdin, Writer stdout, Writer stderr, Provider<Path> codeDirectory) {
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        
        dir = codeDirectory;
    }
    
    @Override
    protected void configure() {
        bind(Reader.class).toInstance(stdin);
        bind(Writer.class).annotatedWith(Stdout.class).toInstance(stdout);
        bind(Writer.class).annotatedWith(Stderr.class).toInstance(stderr);
        
        bind(Path.class).annotatedWith(CodeDirectory.class).toProvider(dir);
        bind(Environment.class).toProvider(EnvironmentProvider.class);
    }
}
