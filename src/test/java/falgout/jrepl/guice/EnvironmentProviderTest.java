package falgout.jrepl.guice;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentClassLoader;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class EnvironmentProviderTest {
    @Inject public Environment env;
    
    @Test
    public void EnvironmentClassLoaderIsThreadContextClassLoader() {
        assertThat(Thread.currentThread().getContextClassLoader(), instanceOf(EnvironmentClassLoader.class));
    }
}
