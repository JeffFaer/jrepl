package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.ClassCompiler.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.GoogleTypes;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratedClassTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void blankClassCanCompile() throws ExecutionException {
        GeneratedClass g = new GeneratedClass(e);
        compile(g);
    }
    
    private Class<?> compile(GeneratedClass clazz) throws ExecutionException {
        Class<?> c = INSTANCE.execute(e, clazz);
        assertEquals(clazz.getName(), c.getSimpleName());
        return c;
    }
    
    @Test
    public void containsEnvironmentVariablesThatAreNeeded() throws ExecutionException, SecurityException,
            ReflectiveOperationException {
        Variable<?> var1 = new Variable<>(true, GoogleTypes.OBJECT, "var1", new Object());
        Variable<?> var2 = new Variable<>(true, GoogleTypes.INT, "var2", 5);
        Variable<?> var3 = new Variable<>(GoogleTypes.OBJECT, "var3", new Object());
        Variable<?> var4 = new Variable<>(true, GoogleTypes.CHAR, "var4", '5');
        Variable<?> var5 = new Variable<>(GoogleTypes.INT, "var5", 5);
        List<Variable<?>> vars = Arrays.asList(var1, var2, var3, var4, var5);
        
        for (Variable<?> var : vars) {
            assertTrue(e.addVariable(var));
        }
        
        Map<GeneratedMethod, Variable<?>> usedVariables = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            Variable<?> var = vars.get(i);
            GeneratedMethod m = new GeneratedMethod(e);
            m.addChild(SourceCode.createReturnStatement(var));
            usedVariables.put(m, var);
        }
        
        GeneratedClass g = new GeneratedClass(e);
        for (GeneratedMethod m : usedVariables.keySet()) {
            g.addChild(m);
        }
        
        Class<?> clazz = compile(g);
        for (Variable<?> var : usedVariables.values()) {
            Field f = var.asField().getTarget(clazz);
            assertNotNull(f);
            if (var.isFinal()) {
                assertTrue(Modifier.isFinal(f.getModifiers()));
            }
        }
        
        vars = new ArrayList<>(vars);
        vars.removeAll(usedVariables.values());
        for (Variable<?> var : vars) {
            try {
                var.asField().getTarget(clazz);
                fail();
            } catch (ReflectiveOperationException e) {}
        }
    }
}
