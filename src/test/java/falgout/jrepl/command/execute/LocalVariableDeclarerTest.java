package falgout.jrepl.command.execute;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.parse.Statements;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules({ TestModule.class, CommandModule.class })
public class LocalVariableDeclarerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public JavaCommandFactory<Optional<? extends List<Variable<?>>>> variableParser = new JavaCommandFactory<>(
            new Pair<>(Statements.INSTANCE, LocalVariableDeclarer.PARSE));

    public List<Variable<?>> parse(String input) throws ExecutionException {
        Optional<? extends List<Variable<?>>> opt = variableParser.execute(e, input);
        return opt.get();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void variablesAreAccessibleFromEnvironment() throws ExecutionException {
        Variable<?> var = parse("int x = 5;").get(0);
        assertThat(e.getVariables(), contains(var));
        assertTrue(e.containsVariable("x"));
        assertEquals(5, e.getVariable("x", GoogleTypes.INT));
    }
    
    @Test(expected = ExecutionException.class)
    public void cannotHaveVoidVariable() throws ExecutionException {
        parse("void x = null;");
    }

    @Test(expected = ExecutionException.class)
    public void cannotAssignStringToPrimitive() throws ExecutionException {
        parse("int x = \"hi\";");
    }

    @Test
    public void canDeclareMultipleVariables() throws ExecutionException {
        List<Variable<?>> vars = parse("int x, y[], z[][];");
        assertEquals(3, vars.size());
        assertEquals(GoogleTypes.INT, vars.get(0).getType());
        TypeToken<?> type = GoogleTypes.addArrays(GoogleTypes.INT, 1);
        assertEquals(type, vars.get(1).getType());
        type = GoogleTypes.addArrays(type, 1);
        assertEquals(type, vars.get(2).getType());
    }
    
    @Test(expected = ExecutionException.class)
    public void genericSafety() throws ExecutionException {
        env.execute("import java.util.*;");
        assertNotNull(parse("List<String> x = Arrays.asList(\"1\");"));
        parse("List<String> y = Arrays.asList(1);");
    }
}