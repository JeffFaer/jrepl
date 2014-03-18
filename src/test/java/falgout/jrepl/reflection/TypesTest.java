package falgout.jrepl.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.TestEnvironment;
import falgout.jrepl.TestModule;
import falgout.jrepl.parser.JavaLexer;
import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.TypeContext;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class TypesTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    private TypeContext parse(String input) {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(input));
        JavaParser parse = new JavaParser(new CommonTokenStream(lex));
        parse.setErrorHandler(new BailErrorStrategy());
        parse.getInterpreter().setPredictionMode(PredictionMode.SLL);
        
        return parse.type();
    }
    
    @Test
    public void returnsPrimitives() throws ClassNotFoundException {
        TypeContext ctx = parse("int");
        assertEquals(TypeToken.of(int.class), Types.getType(e, ctx));
        
        ctx = parse("int[]");
        assertEquals(TypeToken.of(int[].class), Types.getType(e, ctx));
    }
    
    @Test
    public void returnsSimpleObjects() throws ClassNotFoundException {
        TypeContext ctx = parse("Object");
        assertEquals(TypeToken.of(Object.class), Types.getType(e, ctx));
        
        ctx = parse("Object[]");
        assertEquals(TypeToken.of(Object[].class), Types.getType(e, ctx));
    }
    
    @Test
    public void returnsObjectsThatAreImports() throws IOException, ClassNotFoundException {
        try {
            TypeContext ctx = parse("Random[][][]");
            Types.getType(e, ctx);
            fail();
        } catch (ClassNotFoundException e) {}
        
        e.execute("import java.util.*;");
        
        TypeContext ctx = parse("Random[][][]");
        assertEquals(TypeToken.of(Random[][][].class), Types.getType(e, ctx));
    }
    
    @Test
    public void returnsGenericTypes() {
        fail();
    }
}
