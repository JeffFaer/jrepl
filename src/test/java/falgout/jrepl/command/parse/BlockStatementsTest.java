package falgout.jrepl.command.parse;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

import falgout.jrepl.command.parse.JavaParser.BlockStatementContext;

public class BlockStatementsTest {
    public static final BlockStatements blockStatements = new BlockStatements();
    
    private JavaParser getParser(String input) {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        JavaParser parse = new JavaParser(new CommonTokenStream(lex));
        parse.removeErrorListeners();
        parse.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parse.setErrorHandler(new BailErrorStrategy());
        return parse;
    }
    
    @Test
    public void ReturnsOneOrMoreBlockStatements() {
        JavaParser p = getParser("int x = 5; int z = 6;");
        
        List<BlockStatementContext> statements = blockStatements.parse(p).blockStatement();
        assertEquals(2, statements.size());
        
        assertEquals("x", getLocalVariableIdentifier(statements.get(0)));
        
        assertEquals("z", getLocalVariableIdentifier(statements.get(1)));
    }
    
    private String getLocalVariableIdentifier(BlockStatementContext ctx) {
        return ctx.localVariableDeclarationStatement()
                .localVariableDeclaration()
                .variableDeclarators()
                .variableDeclarator()
                .get(0)
                .Identifier()
                .getText();
    }
    
    @Test(expected = ParseCancellationException.class)
    public void FailsWithEmptyStatement() {
        JavaParser p = getParser("");
        blockStatements.parse(p);
    }
}
