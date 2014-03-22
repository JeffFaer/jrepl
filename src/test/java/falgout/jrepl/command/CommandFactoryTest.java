package falgout.jrepl.command;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules({ TestModule.class, CommandModule.class })
public class CommandFactoryTest {
    @Inject @Rule public TestEnvironment env;
    public CommandFactory<?> factory;
    @Inject public Environment e;
    
    @Before
    public void before() {
        factory = env.getFactory();
    }
    
    @Test
    public void parsingErrorsDontTakeUpExtraLines() throws IOException {
        Optional<? extends Command<?>> c = factory.getCommand(e, "int foo");
        assertFalse(c.isPresent());
        String error = env.getError().toString();
        assertThat(error, endsWith("\n"));
        assertThat(error, not(endsWith("\n\n")));
    }

    private void assertCommandExists(String input) {
        assertTrue(factory.getCommand(e, input).isPresent());
    }

    @Test
    public void canParseImports() {
        assertCommandExists("import foo; import foo2.*; import static foo3; import static foo4.*;");
    }
    
    @Test
    public void canParseMultipleStatements() {
        assertCommandExists("int foo = 0; foo++; foo = 5;");
    }
    
    @Test
    public void canParseExpression() {
        assertCommandExists("int foo = 5;");
        assertCommandExists("foo");
    }
    
    @Test
    public void canParseMethod() {
        assertCommandExists("public void foo() {}");
    }
    
    @Test
    public void canParseClass() {
        assertCommandExists("public class Foo {}");
    }
}
