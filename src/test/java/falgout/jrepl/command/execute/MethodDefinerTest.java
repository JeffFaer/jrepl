package falgout.jrepl.command.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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
import falgout.jrepl.command.parse.ClassBodyDeclarations;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class MethodDefinerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public JavaCommandFactory<List<? extends Method>> methodParser = new JavaCommandFactory<>(new Pair<>(
            ClassBodyDeclarations.INSTANCE, (env, input) -> {
                List<BodyDeclaration> l = input.bodyDeclarations();
                return MethodDefiner.INSTANCE.execute(
                        env,
                        l.stream()
                                .filter(d -> d instanceof MethodDeclaration)
                                .map(d -> (MethodDeclaration) d)
                                .collect(Collectors.toList()));
            }));
    
    public List<? extends Method> parse(String methods, String... names) throws ParsingException, ExecutionException {
        List<? extends Method> l = methodParser.execute(e, methods);
        assertEquals(names.length, l.size());
        IntStream.range(0, names.length).forEach(i -> assertEquals(names[i], l.get(i).getName()));
        return l;
    }
    
    @Test
    public void canDefineSimpleMethod() throws ParsingException, ExecutionException {
        parse("public void foo() {}", "foo");
    }
    
    @Test
    public void definedMethodsAreStatic() throws ParsingException, ExecutionException {
        Method m = parse("public void foo(){}", "foo").get(0);
        assertTrue(Modifier.isStatic(m.getModifiers()));
    }
    
    @Test
    public void canDefineMethodWithParameters() throws ParsingException, ExecutionException {
        Method m = parse("public void foo(int x, Object foo) {}", "foo").get(0);
        assertEquals(2, m.getParameterCount());
    }
    
    @Test
    public void canDefineMethodWithReturnType() throws ExecutionException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException {
        Method m = parse("public String foo() { return \"hello world\";}", "foo").get(0);
        assertEquals("hello world", m.invoke(null));
    }
    
    @Test
    public void canDefineMethodWithThrow() throws ParsingException, ExecutionException {
        Method m = parse("public void foo() throws Throwable {}", "foo").get(0);
        assertEquals(1, m.getExceptionTypes().length);
    }
    
    @Test
    public void canDefineMethodWithCustomTypes() throws ExecutionException {
        env.execute("public class ReturnType {}");
        env.execute("public class ParamType1 {}");
        env.execute("public class ParamType2 {}");
        env.execute("public class CustomException extends Throwable {}");
        
        parse("public ReturnType foo(ParamType1 x, ParamType2 y, int primitive) throws CustomException { return null; }",
                "foo");
    }
    
    @Test
    public void canDefineMultipleMethodsAtOnce() throws ParsingException, ExecutionException {
        parse("public void foo(){} public void bar(){}", "foo", "bar");
    }
    
    @Test
    public void canOverloadMethods() throws ParsingException, ExecutionException {
        parse("public void foo() {} public void foo(String s) {}", "foo", "foo");
        parse("public void foo(String s1, String s2) {}", "foo");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotRedefineMethods() throws ParsingException, ExecutionException {
        parse("public void foo() {}", "foo");
        parse("public int foo() { return 1; }", "foo");
    }
}
