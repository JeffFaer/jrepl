package falgout.jrepl.antlr4;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * A more general version of {@link ConsoleErrorListener}.
 * 
 * @author jeffrey
 * 
 */
public class WriterErrorListener extends BaseErrorListener {
    private final PrintWriter writer;
    
    public WriterErrorListener(Writer out) {
        writer = new PrintWriter(out, true);
    }
    
    public WriterErrorListener(OutputStream out) {
        this(out, Charset.defaultCharset());
    }
    
    public WriterErrorListener(OutputStream out, Charset cs) {
        this(new OutputStreamWriter(out, cs));
    }
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        writer.println("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
