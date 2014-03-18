package falgout.jrepl.command;

import static com.google.common.reflect.Types2.addArraysToType;
import static falgout.jrepl.antlr4.ParseTreeUtils.getChildIndex;
import static falgout.jrepl.antlr4.ParseTreeUtils.getChildren;
import static falgout.jrepl.reflection.Types.getType;
import static falgout.jrepl.reflection.Types.isFinal;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.Variable;
import falgout.jrepl.antlr4.WriterErrorListener;
import falgout.jrepl.parser.JavaLexer;
import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.ImportDeclarationContext;
import falgout.jrepl.parser.JavaParser.LocalVariableDeclarationContext;
import falgout.jrepl.parser.JavaParser.VariableDeclaratorContext;
import falgout.jrepl.parser.JavaParser.VariableDeclaratorRestContext;
import falgout.jrepl.parser.JavaParser.VariableInitializerContext;
import falgout.jrepl.reflection.ModifierException;

public class JavaCommand implements Command {
    private static enum ParserRule {
        IMPORT("/importDeclaration") {
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) throws RecognitionException,
                    ParseCancellationException {
                return parser.importDeclaration();
            }
        },
        TOP_LEVEL("/classOrInterfaceDeclaration") {
            // TODO
            // classOrInterfaceDeclaration
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) {
                return parser.classOrInterfaceDeclaration();
            }
        },
        LOCAL("/blockStatement/") {
            @Override
            protected ParserRuleContext tryInvoke(JavaParser parser) {
                return parser.blockStatement();
            }
        },
        METHOD("/classBodyDeclaration") {
            // TODO
            // semicolon
            // modifier* memberDecl
            // methodOrField (won't be field, LOCAL picks it up first)
            // voidMethodDeclarator
            // constructor (don't want this)
            // genericMethodOrConstructor (don' want constructor
            // static? block
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
    
    private static enum Type {
        IMPORT(ParserRule.IMPORT, "") {
            @Override
            public boolean execute(Environment e, ParseTree command) throws IOException {
                e.getImports().add(Import.create((ImportDeclarationContext) command));
                return true;
            }
        },
        LOCAL_VARAIBLE_DECLARATION(ParserRule.LOCAL, "localVariableDeclarationStatement/localVariableDeclaration") {
            @Override
            public boolean execute(Environment e, ParseTree command) throws IOException {
                LocalVariableDeclarationContext ctx = (LocalVariableDeclarationContext) command;
                
                boolean _final;
                try {
                    _final = isFinal(ctx.variableModifier());
                } catch (ModifierException e1) {
                    e.getError().println(e1.getMessage());
                    return false;
                }
                
                TypeToken<?> baseType;
                try {
                    baseType = getType(e, ctx.type());
                } catch (ClassNotFoundException e1) {
                    e.getError().println(e1.getMessage());
                    return false;
                }
                
                Map<String, Variable<?>> variables = new LinkedHashMap<>();
                for (VariableDeclaratorContext var : ctx.variableDeclarators().variableDeclarator()) {
                    String name = var.Identifier().getText();
                    
                    if (e.containsVariable(name)) {
                        e.getError().printf("%s already exists.\n", name);
                        return false;
                    }
                    
                    VariableDeclaratorRestContext rest = var.variableDeclaratorRest();
                    int extraArrays = rest.L_BRACKET().size();
                    TypeToken<?> varType = addArraysToType(baseType, extraArrays);
                    
                    VariableInitializerContext init = rest.variableInitializer();
                    Object value = null;
                    if (init != null) {
                        // TODO
                        // value = evaluate(init);
                    }
                    
                    variables.put(name, new Variable<>(value, varType, _final));
                }
                
                if (!e.addVariables(variables)) {
                    throw new Error("Something went horribly wrong");
                }
                
                return true;
            }
        },
        STATEMENT(ParserRule.LOCAL, "statement") {
            @Override
            public boolean execute(Environment e, ParseTree command) throws IOException {
                // TODO Auto-generated method stub
                return true;
            }
        };
        
        private final ParserRule parent;
        private final String xPathPredicate;
        
        private Type(ParserRule parent, String suffix) {
            this.parent = parent;
            xPathPredicate = parent.getPrefix() + suffix;
        }
        
        public abstract boolean execute(Environment e, ParseTree command) throws IOException;
        
        public static Map<ParseTree, Type> split(final ParseResult result, JavaParser parser) {
            Map<ParseTree, Type> split = new TreeMap<>(new Comparator<ParseTree>() {
                @Override
                public int compare(ParseTree o1, ParseTree o2) {
                    // sort based on occurrence in tree (left to first)
                    int p1 = getChildIndex(result.ctx, o1);
                    int p2 = getChildIndex(result.ctx, o2);
                    return Integer.compare(p1, p2);
                }
            });
            for (Type t : Type.values()) {
                if (result.method == t.parent) {
                    for (ParseTree c : XPath.findAll(result.ctx, t.xPathPredicate, parser)) {
                        if (split.containsKey(c)) {
                            throw new Error("ruh roh");
                        }
                        split.put(c, t);
                    }
                }
            }
            
            return split;
        }
    }
    
    private static class ParseResult {
        public final ParserRuleContext ctx;
        public final ParserRule method;
        
        public ParseResult(ParserRuleContext ctx, ParserRule method) {
            this.ctx = ctx;
            this.method = method;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ParseResult [method=");
            builder.append(method);
            builder.append("]");
            return builder.toString();
        }
    }
    
    private final ParseTree ctx;
    private final Type type;
    
    private JavaCommand(ParseTree ctx, Type type) {
        this.ctx = ctx;
        this.type = type;
    }
    
    @Override
    public boolean execute(Environment e) throws IOException {
        return type.execute(e, ctx);
    }
    
    public static CompoundCommand<JavaCommand> getCommand(String input, Writer err) throws IOException {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        JavaParser parser = new JavaParser(new CommonTokenStream(lex));
        parser.removeErrorListeners();
        
        ParseResult r = stageOne(parser);
        if (r == null) {
            r = stageTwo(parser, err);
        }
        
        if (r == null) {
            return null;
        }
        
        List<JavaCommand> commands = new ArrayList<>();
        for (Entry<ParseTree, Type> e : Type.split(r, parser).entrySet()) {
            commands.add(new JavaCommand(e.getKey(), e.getValue()));
        }
        
        return new CompoundCommand<>(commands);
    }
    
    private static ParseResult tryParse(JavaParser parser, Predicate<? super ParserRuleContext> ret) {
        for (ParserRule m : ParserRule.values()) {
            ParserRuleContext ctx = m.invoke(parser);
            
            if (ctx != null && ret.apply(ctx)) {
                return new ParseResult(ctx, m);
            }
            
            parser.reset();
        }
        
        return null;
    }
    
    private static ParseResult stageOne(JavaParser parser) {
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setErrorHandler(new BailErrorStrategy());
        
        return tryParse(parser, Predicates.alwaysTrue());
    }
    
    private static ParseResult stageTwo(JavaParser parser, Writer err) throws IOException {
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.setErrorHandler(new DefaultErrorStrategy());
        
        final CharArrayWriter sink = new CharArrayWriter();
        parser.addErrorListener(new WriterErrorListener(sink));
        
        final Set<String> errors = new LinkedHashSet<>();
        final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);
        
        ParseResult ctx = tryParse(parser, new Predicate<ParserRuleContext>() {
            @Override
            public boolean apply(ParserRuleContext input) {
                int numErrors = getChildren(input, ErrorNode.class).size();
                if (numErrors == 0) {
                    return true;
                }
                
                // only keep the error message(s) for the ParseTrees which have
                // the best heuristic. The heuristic we're currently using is
                // which parse tree has the fewest errors
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
