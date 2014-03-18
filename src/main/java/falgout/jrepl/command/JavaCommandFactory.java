package falgout.jrepl.command;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
import com.google.common.collect.ImmutableList;

import falgout.jrepl.Environment;
import falgout.jrepl.antlr4.ParseTreeUtils;
import falgout.jrepl.antlr4.WriterErrorListener;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.execute.ImportExecutor;
import falgout.jrepl.command.execute.LocalVariable;
import falgout.jrepl.command.parse.BlockStatements;
import falgout.jrepl.command.parse.ImportDeclaration;
import falgout.jrepl.command.parse.JavaParserRule;
import falgout.jrepl.parser.JavaLexer;
import falgout.jrepl.parser.JavaParser;

public class JavaCommandFactory implements CommandFactory {
    private static class IntermediateWrapper<I extends ParserRuleContext> implements JavaParserRule<I>, Command {
        private final JavaParserRule<I> delegate;
        private final Set<Executor<I>> executors;
        private I intermediary;
        
        @SafeVarargs
        public IntermediateWrapper(JavaParserRule<I> parser, Executor<I>... executors) {
            this.delegate = parser;
            this.executors = new LinkedHashSet<>(Arrays.asList(executors));
        }
        
        @Override
        public I parse(JavaParser parser) {
            intermediary = delegate.parse(parser);
            return intermediary;
        }
        
        @Override
        public boolean execute(Environment env) throws IOException {
            if (intermediary == null) {
                throw new AssertionError();
            }
            
            for (Executor<I> e : executors) {
                if (!e.execute(env, intermediary)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private final List<IntermediateWrapper<?>> wrappers;
    
    public JavaCommandFactory() {
        wrappers = ImmutableList.<IntermediateWrapper<?>> builder()
                .add(new IntermediateWrapper<>(new ImportDeclaration(), new ImportExecutor()))
                // TODO
                // .add(new IntermediateWrapper<>(new
                // ClassOrInterfaceDeclaration(), executors))
                .add(new IntermediateWrapper<>(new BlockStatements(), new LocalVariable()))
                // TODO
                // .add(new IntermediateWrapper<>(new ClassBodyDeclaration(),
                // executors))
                .build();
    }
    
    @Override
    public Command getCommand(Environment env, String input) {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        JavaParser parser = new JavaParser(new CommonTokenStream(lex));
        parser.removeErrorListeners();
        
        IntermediateWrapper<?> i = stageOne(parser);
        if (i == null) {
            i = stageTwo(env, parser);
            if (i == null) {
                return null;
            }
        }
        
        return i;
    }
    
    private IntermediateWrapper<?> tryParse(JavaParser parser, Predicate<? super ParserRuleContext> ret) {
        for (IntermediateWrapper<?> i : wrappers) {
            try {
                ParserRuleContext o = i.parse(parser);
                
                if (o != null && ret.apply(o)) {
                    return i;
                }
            } catch (ParseCancellationException | RecognitionException e) {
                // either we're in stage one where we want to ignore these
                // or we're in stage two where these won't be thrown
            }
            
            parser.reset();
        }
        
        return null;
    }
    
    private IntermediateWrapper<?> stageOne(JavaParser parser) {
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setErrorHandler(new BailErrorStrategy());
        
        return tryParse(parser, Predicates.alwaysTrue());
    }
    
    private IntermediateWrapper<?> stageTwo(Environment env, JavaParser parser) {
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.setErrorHandler(new DefaultErrorStrategy());
        
        final CharArrayWriter sink = new CharArrayWriter();
        parser.addErrorListener(new WriterErrorListener(sink));
        
        final Set<String> errors = new LinkedHashSet<>();
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        
        IntermediateWrapper<?> i = tryParse(parser, new Predicate<ParserRuleContext>() {
            @Override
            public boolean apply(ParserRuleContext input) {
                int numErrors = ParseTreeUtils.getChildren(input, ErrorNode.class).size();
                if (numErrors == 0) {
                    return true;
                }
                
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
        
        if (i == null) {
            for (String error : errors) {
                env.getError().write(error);
            }
        }
        
        return i;
    }
}
