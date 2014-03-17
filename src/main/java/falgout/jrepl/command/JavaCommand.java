package falgout.jrepl.command;

import static falgout.jrepl.antlr4.ParseTreeUtils.getChildren;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import falgout.jrepl.Environment;
import falgout.jrepl.antlr4.WriterErrorListener;
import falgout.jrepl.parser.JavaLexer;
import falgout.jrepl.parser.JavaParser;

public class JavaCommand implements Command {
    private static enum ParserRule {
        TOP_LEVEL("/classOrInterfaceDeclaration") {
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) {
                return parser.classOrInterfaceDeclaration();
            }
        },
        LOCAL("/blockStatements/blockStatement/") {
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) {
                return parser.blockStatements();
            }
        },
        METHOD("/classBodyDeclaration") {
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) {
                return parser.classBodyDeclaration();
            }
        };
        
        private final String prefix;
        
        private ParserRule(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public ParserRuleContext invoke(JavaParser parser) {
            try {
                return tryInvoke(parser);
            } catch (ParseCancellationException | RecognitionException e) {
                // either we're in stage 1 where we want to ignore these
                // exceptions or we're in stage 2 where these exceptions won't
                // be thrown
            }
            
            return null;
        }
        
        protected abstract ParserRuleContext tryInvoke(JavaParser parser) throws RecognitionException,
                ParseCancellationException;
    }
    
    public static enum Type {
        LOCAL_VARAIBLE_DECLARATION(ParserRule.LOCAL, "localVariableDeclarationStatement") {
            @Override
            public boolean execute(Environment e, ParseTree root) throws IOException {
                // TODO
                return true;
            }
        };
        
        private final String xPathPredicate;
        
        private Type(ParserRule parent, String suffix) {
            xPathPredicate = parent.getPrefix() + suffix;
        }
        
        public abstract boolean execute(Environment e, ParseTree root) throws IOException;
        
        private static Map<ParseTree, Type> split(ParserRuleContext ctx, JavaParser parser) {
            Map<ParseTree, Type> split = new LinkedHashMap<>();
            for (Type t : Type.values()) {
                for (ParseTree c : XPath.findAll(ctx, t.xPathPredicate, parser)) {
                    if (split.containsKey(c)) {
                        throw new Error("ruh roh");
                    }
                    
                    split.put(c, t);
                }
            }
            // TODO
            // if (ctx instanceof ClassOrInterfaceDeclarationContext) {
            // // TODO off to the compiler with you!
            // } else if (ctx instanceof BlockStatementsContext) {
            // for (BlockStatementContext b : ((BlockStatementsContext)
            // ctx).blockStatement()) {
            // LocalVariableDeclarationStatementContext local =
            // b.localVariableDeclarationStatement();
            // if (local != null) {
            // evaluate(local.localVariableDeclaration());
            // } else {
            // evaluate(b.statement());
            // }
            // }
            // // localVariableDeclarations or
            // // statements
            // } else {
            // // classBodyDeclaration:
            // // method
            // // voidMethod
            // // block
            // // TODO filter out constructors
            // }s
            return split;
        }
    }
    
    private final ParseTree ctx;
    private final Type type;
    
    private JavaCommand(ParseTree ctx, Type type) {
        this.ctx = ctx;
        this.type = type;
    }
    
    public Type getCommandType() {
        return type;
    }
    
    @Override
    public boolean execute(Environment e) throws IOException {
        return type.execute(e, ctx);
    }
    
    public static Command getCommand(String input, Writer err) throws IOException {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        JavaParser parser = new JavaParser(new CommonTokenStream(lex));
        parser.removeErrorListeners();
        
        ParserRuleContext ctx = stageOne(parser);
        if (ctx == null) {
            ctx = stageTwo(parser, err);
        }
        
        if (ctx == null) {
            return null;
        }
        
        List<JavaCommand> commands = new ArrayList<>();
        for (Entry<ParseTree, Type> e : Type.split(ctx, parser).entrySet()) {
            commands.add(new JavaCommand(e.getKey(), e.getValue()));
        }
        
        return commands.size() == 1 ? commands.get(0) : new CompoundCommand(commands);
    }
    
    private static ParserRuleContext tryParse(JavaParser parser, Predicate<? super ParserRuleContext> ret) {
        for (ParserRule m : ParserRule.values()) {
            ParserRuleContext ctx = m.invoke(parser);
            
            if (ctx != null && ret.apply(ctx)) {
                return ctx;
            }
            
            parser.reset();
        }
        
        return null;
    }
    
    private static ParserRuleContext stageOne(JavaParser parser) {
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setErrorHandler(new BailErrorStrategy());
        
        return tryParse(parser, Predicates.alwaysTrue());
    }
    
    private static ParserRuleContext stageTwo(JavaParser parser, Writer err) throws IOException {
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.setErrorHandler(new DefaultErrorStrategy());
        
        final CharArrayWriter sink = new CharArrayWriter();
        parser.addErrorListener(new WriterErrorListener(sink));
        
        final Set<String> errors = new LinkedHashSet<>();
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        
        ParserRuleContext ctx = tryParse(parser, new Predicate<ParserRuleContext>() {
            @Override
            public boolean apply(ParserRuleContext input) {
                int numErrors = getChildren(input, ErrorNode.class).size();
                if (numErrors == 0) {
                    return true;
                }
                
                // only keep the error message(s) for the ParseTrees which have
                // the fewest errors (which I'm using as a metric to determine
                // which ParseTree is closest to the intended input)
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
