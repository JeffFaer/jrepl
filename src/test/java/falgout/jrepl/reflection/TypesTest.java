package falgout.jrepl.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
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
    public ClassLoader cl;
    
    @Before
    public void before() {
        cl = e.getImportClassLoader();
    }
    
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
        assertEquals(TypeToken.of(int.class), Types.getType(cl, ctx));
        
        ctx = parse("int[]");
        assertEquals(TypeToken.of(int[].class), Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsSimpleObjects() throws ClassNotFoundException {
        TypeContext ctx = parse("Object");
        assertEquals(TypeToken.of(Object.class), Types.getType(cl, ctx));
        
        ctx = parse("Object[]");
        assertEquals(TypeToken.of(Object[].class), Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsObjectsThatAreImports() throws IOException, ClassNotFoundException {
        try {
            TypeContext ctx = parse("Random[][][]");
            Types.getType(cl, ctx);
            fail();
        } catch (ClassNotFoundException e) {}
        
        e.execute("import java.util.*;");
        
        TypeContext ctx = parse("Random[][][]");
        assertEquals(TypeToken.of(Random[][][].class), Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsSimpleGenericType() throws IOException, ClassNotFoundException {
        e.execute("import java.util.*;");
        env.assertNoErrors();
        
        TypeContext ctx = parse("List<String>");
        assertEquals(new TypeToken<List<String>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsCompoundGenericTypes() throws IOException, ClassNotFoundException {
        e.execute("import java.util.*;");
        env.assertNoErrors();
        
        TypeContext ctx = parse("Map<List<String>, Set<int[]>>");
        assertEquals(new TypeToken<Map<List<String>, Set<int[]>>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsWildcards() throws IOException {
        fail();
        // e.execute("import java.util.*;");
        // env.assertNoErrors();
        //
        // TypeContext ctx = parse("List<? extends Number>");
        // assertEquals(new TypeToken<? extends Number>() {
        // }, Types.getType(cl, ctx));
    }
}
