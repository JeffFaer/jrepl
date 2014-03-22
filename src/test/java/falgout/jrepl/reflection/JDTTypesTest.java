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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.command.CommandModule;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules({ TestModule.class, CommandModule.class })
public class JDTTypesTest {
    @Inject @Rule public TestEnvironment env;
    
    private Type parse(String input) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        parser.setSource((input + " foo;").toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        
        Block block = (Block) parser.createAST(null);
        VariableDeclarationStatement statement = (VariableDeclarationStatement) block.statements().get(0);
        
        return statement.getType();
    }

    @Test
    public void returnsPrimitives() throws ClassNotFoundException {
        Type type = parse("int");
        assertEquals(TypeToken.of(int.class), JDTTypes.getType(type));

        type = parse("int[]");
        assertEquals(TypeToken.of(int[].class), JDTTypes.getType(type));
    }

    @Test
    public void returnsSimpleObjects() throws ClassNotFoundException {
        Type type = parse("Object");
        assertEquals(TypeToken.of(Object.class), JDTTypes.getType(type));

        type = parse("Object[]");
        assertEquals(TypeToken.of(Object[].class), JDTTypes.getType(type));
    }

    @Test
    public void returnsObjectsThatAreImports() throws IOException, ClassNotFoundException {
        try {
            Type type = parse("Random[][][]");
            JDTTypes.getType(type);
            fail();
        } catch (ClassNotFoundException e) {}

        env.executeNoErrors("import java.util.*;");

        Type type = parse("Random[][][]");
        assertEquals(TypeToken.of(Random[][][].class), JDTTypes.getType(type));
    }

    @Test
    public void returnsSimpleGenericType() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.util.*;");

        Type type = parse("List<String>");
        assertEquals(new TypeToken<List<String>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, JDTTypes.getType(type));
    }

    @Test
    public void returnsCompoundGenericTypes() throws IOException, ClassNotFoundException {
        env.executeNoErrors("import java.util.*;");

        Type type = parse("Map<List<String>, Set<int[]>>");
        assertEquals(new TypeToken<Map<List<String>, Set<int[]>>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, JDTTypes.getType(type));
    }

    @Test(expected = ClassNotFoundException.class)
    public void invalidGenericTypesThrowClassNotFound() throws ClassNotFoundException {
        JDTTypes.getType(parse("Object<Object, Object>"));
    }

    @Test
    public void returnsWildcards() throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException {
        env.executeNoErrors("import java.util.*;");

        Type type = parse("List<? extends Number>");
        TypeToken<?> typeToken = JDTTypes.getType(type);

        assertTrue(typeToken.isAssignableFrom(new TypeToken<List<Integer>>() {
            private static final long serialVersionUID = 1119604468904475049L;
        }));
        assertTrue(typeToken.isAssignableFrom(new TypeToken<ArrayList<Number>>() {
            private static final long serialVersionUID = -7833191612055147550L;
        }));

        type = parse("List<? super Number>");
        typeToken = JDTTypes.getType(type);

        assertTrue(typeToken.isAssignableFrom(new TypeToken<List<Object>>() {
            private static final long serialVersionUID = -2180513927254148611L;
        }));
    }
}
