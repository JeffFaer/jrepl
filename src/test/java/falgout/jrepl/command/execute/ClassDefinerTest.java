package falgout.jrepl.command.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.ParsingException;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.NestedClass;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class ClassDefinerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public JavaCommandFactory<List<? extends NestedClass<?>>> typeParser = new JavaCommandFactory<>(new Pair<>(
            ClassDeclaration.INSTANCE, (env, input) -> ClassDefiner.INSTANCE.execute(env, input.types())));
    
    public List<Class<?>> parse(String input, String... names) throws ParsingException, ExecutionException {
        List<? extends NestedClass<?>> nested = typeParser.execute(e, input);
        assertEquals(names.length, nested.size());
        List<Class<?>> classes = new ArrayList<>(nested.size());
        for (int i = 0; i < nested.size(); i++) {
            Class<?> clazz = nested.get(i).getDeclaredClass();
            classes.add(clazz);
            assertEquals(names[i], clazz.getSimpleName());
        }
        return classes;
    }
    
    @Test
    public void canDefineSimpleClass() throws ParsingException, ExecutionException {
        parse("public class Foo {}", "Foo");
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws ParsingException, ExecutionException {
        env.execute("Object var1 = new Object();");
        
        parse("public class Foo { { System.out.println(var1); } }", "Foo");
    }
    
    @Test
    public void canAccessEnvironmentClasses() throws ParsingException, ExecutionException {
        parse("public class Foo {}", "Foo");
        parse("public class Foo2 { { new Foo(); } }", "Foo2");
    }
    
    @Test
    public void canAccessEnvironmentMethods() throws ParsingException, ExecutionException {
        env.execute("public void foo() {}");
        parse("public class Foo { { foo(); } }", "Foo");
    }
    
    @Test
    public void canAccessOverloadedEnvironmentMethods() throws ParsingException, ExecutionException {
        env.execute("public void foo() {}");
        env.execute("public void foo(String s) {}");
        
        parse("public class Foo { { foo(); foo(null); } }", "Foo");
    }
    
    @Test
    public void canAccessEnvironmentImports() throws ExecutionException {
        env.execute("import java.util.*;");
        parse("public class Foo { List<String> bar; }", "Foo");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotDeclareDuplicateClass() throws ParsingException, ExecutionException {
        parse("public class Foo {}", "Foo");
        parse("public class Foo { public static int different; }", "Foo");
    }
    
    @Test
    public void definedClassesAreStatic() throws ParsingException, ExecutionException {
        Class<?> clazz = parse("public class Foo {}", "Foo").get(0);
        assertTrue(Modifier.isStatic(clazz.getModifiers()));
    }
    
    @Test
    public void canDefineInterfaces() throws ParsingException, ExecutionException {
        Class<?> clazz = parse("public interface Foo {}", "Foo").get(0);
        assertTrue(clazz.isInterface());
    }
    
    @Test
    public void canDefineEnums() throws ParsingException, ExecutionException {
        Class<?> clazz = parse("public enum Foo { INSTANCE; }", "Foo").get(0);
        assertTrue(clazz.isEnum());
        assertEquals(1, clazz.getEnumConstants().length);
        assertEquals("INSTANCE", clazz.getEnumConstants()[0].toString());
    }
    
    @Test
    public void canDefineAnnotations() throws ParsingException, ExecutionException {
        Class<?> clazz = parse("public @interface Foo {}", "Foo").get(0);
        assertTrue(clazz.isAnnotation());
    }
}
