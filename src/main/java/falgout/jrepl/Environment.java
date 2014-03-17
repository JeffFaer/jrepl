package falgout.jrepl;

import static falgout.jrepl.antlr4.ParseTreeUtils.getChildren;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.reflect.TypeToken;

import falgout.jrepl.antlr4.WriterErrorListener;
import falgout.jrepl.parser.JavaLexer;
import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.BlockStatementContext;
import falgout.jrepl.parser.JavaParser.BlockStatementsContext;
import falgout.jrepl.parser.JavaParser.ClassOrInterfaceDeclarationContext;
import falgout.jrepl.parser.JavaParser.LocalVariableDeclarationContext;
import falgout.jrepl.parser.JavaParser.LocalVariableDeclarationStatementContext;
import falgout.jrepl.parser.JavaParser.StatementContext;

public class Environment {
    private static final List<Method> PARSE_ORDER;
    static {
        List<Method> m = new ArrayList<>();
        for (String name : new String[] { "classOrInterfaceDeclaration", "blockStatements", "classBodyDeclaration" }) {
            try {
                m.add(JavaParser.class.getMethod(name));
            } catch (NoSuchMethodException e) {
                throw new Error(e);
            }
        }
        PARSE_ORDER = Collections.unmodifiableList(m);
    }
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();
    
    public Environment() {}
    
    public <T> T get(String variableName, TypeToken<T> type) {
        Variable<?> var = variables.get(variableName);
        if (var != null && type.isAssignableFrom(var.getType())) {
            return (T) var.get();
        }
        return null;
    }
    
    public <T> Map<String, ? extends T> get(TypeToken<T> type) {
        Map<String, T> ret = new LinkedHashMap<>();
        for (Entry<String, Variable<?>> e : variables.entrySet()) {
            if (type.isAssignableFrom(e.getValue().getType())) {
                ret.put(e.getKey(), (T) e.getValue().get());
            }
        }
        
        return ret;
    }
    
    public void execute(String input, OutputStream out, OutputStream err) throws IOException {
        execute(input, new OutputStreamWriter(out), new OutputStreamWriter(err));
    }
    
    public void execute(String input, Writer out, Writer err) throws IOException {
        try {
            ParserRuleContext ctx = parse(input, err);
            if (ctx == null) {
                return;
            }
            
            if (ctx instanceof ClassOrInterfaceDeclarationContext) {
                // TODO off to the compiler with you!
            } else if (ctx instanceof BlockStatementsContext) {
                for (BlockStatementContext b : ((BlockStatementsContext) ctx).blockStatement()) {
                    LocalVariableDeclarationStatementContext local = b.localVariableDeclarationStatement();
                    if (local != null) {
                        evaluate(local.localVariableDeclaration());
                    } else {
                        evaluate(b.statement());
                    }
                }
                // localVariableDeclarations or
                // statements
            } else {
                // classBodyDeclaration:
                // method
                // voidMethod
                // block
                // TODO filter out constructors
            }
            
        } finally {
            out.flush();
            err.flush();
        }
    }
    
    private void evaluate(LocalVariableDeclarationContext localVariableDeclaration) {
        // TODO Auto-generated method stub
        
    }
    
    private void evaluate(StatementContext statement) {
        Objects.requireNonNull(statement);
        // TODO
        throw new RuntimeException("Not yet implemented");
    }
    
    private ParserRuleContext parse(String input, Writer err) throws IOException {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        final JavaParser parser = new JavaParser(new CommonTokenStream(lex));
        parser.removeErrorListeners();
        
        ParserRuleContext ctx = stageOne(parser);
        if (ctx == null) {
            ctx = stageTwo(parser, err);
        }
        
        return ctx;
    }
    
    private ParserRuleContext tryParse(JavaParser parse, Predicate<? super ParserRuleContext> ret) {
        for (Method m : PARSE_ORDER) {
            ParserRuleContext ctx = null;
            try {
                ctx = (ParserRuleContext) m.invoke(parse);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            } catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                if (!(t instanceof RecognitionException) && !(t instanceof ParseCancellationException)) {
                    throw new Error(e);
                }
            }
            
            if (ctx != null && ret.apply(ctx)) {
                return ctx;
            }
            
            parse.reset();
        }
        
        return null;
    }
    
    private ParserRuleContext stageOne(JavaParser parse) {
        parse.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parse.setErrorHandler(new BailErrorStrategy());
        
        return tryParse(parse, Predicates.alwaysTrue());
    }
    
    private ParserRuleContext stageTwo(JavaParser parse, Writer err) throws IOException {
        parse.getInterpreter().setPredictionMode(PredictionMode.LL);
        parse.setErrorHandler(new DefaultErrorStrategy());
        
        final CharArrayWriter sink = new CharArrayWriter();
        parse.addErrorListener(new WriterErrorListener(sink));
        
        final Set<String> errors = new LinkedHashSet<>();
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        
        ParserRuleContext ctx = tryParse(parse, new Predicate<ParserRuleContext>() {
            @Override
            public boolean apply(ParserRuleContext input) {
                int numErrors = getChildren(input, ErrorNode.class).size();
                if (numErrors == 0) {
                    return true;
                }
                
                // only keep the error message for the ParseTrees which have the
                // fewest errors (which I assume means they are the closest to
                // matching the input)
                if (numErrors < min.get()) {
                    min.set(numErrors);
                    errors.clear();
                }
                if (numErrors <= min.get()) {
                    errors.add(sink.toString());
                }
                
                sink.reset();
                
                return false;
            }
        });
        
        if (ctx == null) {
            for (String error : errors) {
                err.write(error);
            }
        }
        
        return ctx;
    }
}
