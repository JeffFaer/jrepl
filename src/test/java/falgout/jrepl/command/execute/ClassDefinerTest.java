package falgout.jrepl.command.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.ParsingException;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules({ TestModule.class, CommandModule.class })
public class ClassDefinerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public JavaCommandFactory<Optional<? extends List<Class<?>>>> typeParser = new JavaCommandFactory<>(new Pair<>(
            ClassDeclaration.INSTANCE, ClassDefiner.PARSE));
    
    public List<Class<?>> parse(String input, String... names) throws ParsingException, ExecutionException {
        Optional<? extends List<Class<?>>> opt = typeParser.execute(e, input);
        assertTrue(opt.isPresent());
        List<Class<?>> classes = opt.get();
        for (int i = 0; i < classes.size(); i++) {
            assertEquals(names[i], classes.get(i).getSimpleName());
        }
        return classes;
    }
    
    @Test
    public void canDefineSimpleClass() throws ParsingException, ExecutionException {
        parse("public class Foo {}", "Foo");
    }
    
    @Test
    public void canAccessEnvironmentVariables() throws ParsingException, ExecutionException {
        Variable<?> var1 = new Variable<>(false, GoogleTypes.OBJECT, "var1", new Object());
        e.addVariable(var1);
        
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
        // TODO
    }
    
    @Test
    public void canAccessEnvironmentImports() throws ExecutionException {
        env.execute("import java.util.*;");
        parse("public class Foo { List<String> bar; }", "Foo");
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotDeclareDuplicateClass() throws ParsingException, ExecutionException {
        parse("public class Foo {}", "Foo");
        parse("public class Foo {}", "Foo");
    }
}
