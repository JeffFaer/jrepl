package falgout.jrepl.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
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

import falgout.jrepl.TestEnvironment;
import falgout.jrepl.TestModule;
import falgout.jrepl.command.parse.JavaLexer;
import falgout.jrepl.command.parse.JavaParser;
import falgout.jrepl.command.parse.JavaParser.TypeContext;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class TypesTest {
    @Inject @Rule public TestEnvironment env;
    public ClassLoader cl;
    
    @Before
    public void before() {
        cl = env.getEnvironment().getImportClassLoader();
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
        
        env.executeNoErrors("import java.util.*;");
        
        TypeContext ctx = parse("Random[][][]");
        assertEquals(TypeToken.of(Random[][][].class), Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsSimpleGenericType() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.util.*;");
        
        TypeContext ctx = parse("List<String>");
        assertEquals(new TypeToken<List<String>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, Types.getType(cl, ctx));
    }
    
    @Test
    public void returnsCompoundGenericTypes() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.util.*;");
        
        TypeContext ctx = parse("Map<List<String>, Set<int[]>>");
        assertEquals(new TypeToken<Map<List<String>, Set<int[]>>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, Types.getType(cl, ctx));
    }
    
    @Test(expected = ClassNotFoundException.class)
    public void invalidGenericTypesThrowClassNotFound() throws ClassNotFoundException {
        Types.getType(cl, parse("Object<Object, Object>"));
    }
    
    @Test
    public void returnsWildcards() throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
        env.executeNoErrors("import java.util.*;");
        
        TypeContext ctx = parse("List<? extends Number>");
        TypeToken<?> type = Types.getType(cl, ctx);
        
        assertTrue(type.isAssignableFrom(new TypeToken<List<Integer>>() {
            private static final long serialVersionUID = 1119604468904475049L;
        }));
        assertTrue(type.isAssignableFrom(new TypeToken<ArrayList<Number>>() {
            private static final long serialVersionUID = -7833191612055147550L;
        }));
        
        ctx = parse("List<? super Number>");
        type = Types.getType(cl, ctx);
        
        assertTrue(type.isAssignableFrom(new TypeToken<List<Object>>() {
            private static final long serialVersionUID = -2180513927254148611L;
        }));
    }
}
