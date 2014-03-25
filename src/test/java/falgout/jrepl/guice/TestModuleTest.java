package falgout.jrepl.guice;

import static org.junit.Assert.assertSame;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class TestModuleTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    
    @Test
    public void InjectedTestEnvironmentAndEnvironmentReferenceSameObject() {
        assertSame(e, env.getEnvironment());
    }
}
