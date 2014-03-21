package falgout.jrepl.command.execute.codegen;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;
import falgout.jrepl.reflection.Types;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class GeneratorModuleTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public Injector i;

    public Variable<?> uninitialized = new Variable<>(Types.OBJECT, "foo");
    public Variable<?> initialized = new Variable<>(Types.OBJECT, "foo2", true);
    
    @Before
    public void before() {
        e.addVariable(uninitialized);
        e.addVariable(initialized);
        
        i = Guice.createInjector(new GeneratorModule(e));
    }

    @Test
    public void doesNotCreateBindingForUninitializedVariable() {
        assertEquals(1, getNamedBindings().size());
    }

    private Set<Key<?>> getNamedBindings() {
        return i.getAllBindings()
                .keySet()
                .stream()
                .filter(key -> key.getAnnotation() instanceof Named)
                .collect(Collectors.toSet());
    }
}
