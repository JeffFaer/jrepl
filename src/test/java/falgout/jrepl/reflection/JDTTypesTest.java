package falgout.jrepl.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.parse.Statements;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class JDTTypesTest {
    @Inject @Rule public TestEnvironment env;
    public JavaCommandFactory<Optional<Type>> typeParser;
    
    @Before
    public void before() {
        Executor<org.eclipse.jdt.core.dom.Block, Optional<Type>> exec = (env, input) -> Optional.of(((VariableDeclarationStatement) input.statements()
                .get(0)).getType());
        typeParser = new JavaCommandFactory<>(new Pair<>(Statements.INSTANCE, exec));
    }
    
    private Type parse(String input) throws ExecutionException {
        Optional<Type> opt = typeParser.execute(env.getEnvironment(), input + " foo;");
        assertTrue(opt.isPresent());
        return opt.get();
    }
    
    @Test
    public void returnsPrimitives() throws ExecutionException, ReflectiveOperationException {
        Type type = parse("int");
        assertEquals(TypeToken.of(int.class), JDTTypes.getType(type));
        
        type = parse("int[]");
        assertEquals(TypeToken.of(int[].class), JDTTypes.getType(type));
    }
    
    @Test
    public void returnsSimpleObjects() throws ExecutionException, ReflectiveOperationException {
        Type type = parse("Object");
        assertEquals(TypeToken.of(Object.class), JDTTypes.getType(type));
        
        type = parse("Object[]");
        assertEquals(TypeToken.of(Object[].class), JDTTypes.getType(type));
    }
    
    @Test
    public void returnsObjectsThatAreImports() throws ExecutionException, ReflectiveOperationException {
        try {
            Type type = parse("Random[][][]");
            JDTTypes.getType(type);
            fail();
        } catch (ClassNotFoundException e) {}
        
        env.execute("import java.util.*;");
        
        Type type = parse("Random[][][]");
        assertEquals(TypeToken.of(Random[][][].class), JDTTypes.getType(type));
    }
    
    @Test
    public void returnsSimpleGenericType() throws ExecutionException, ReflectiveOperationException {
        env.execute("import java.util.*;");
        
        Type type = parse("List<String>");
        assertEquals(new TypeToken<List<String>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, JDTTypes.getType(type));
    }
    
    @Test
    public void returnsCompoundGenericTypes() throws ExecutionException, ReflectiveOperationException {
        env.execute("import java.util.*;");
        
        Type type = parse("Map<List<String>, Set<int[]>>");
        assertEquals(new TypeToken<Map<List<String>, Set<int[]>>>() {
            private static final long serialVersionUID = 6065261116676637497L;
        }, JDTTypes.getType(type));
    }
    
    @Test(expected = ReflectiveOperationException.class)
    public void invalidGenericTypesThrowClassNotFound() throws ExecutionException, ReflectiveOperationException {
        JDTTypes.getType(parse("Object<Object, Object>"));
    }
    
    @Test
    public void returnsWildcards() throws ExecutionException, ReflectiveOperationException {
        env.execute("import java.util.*;");
        
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
