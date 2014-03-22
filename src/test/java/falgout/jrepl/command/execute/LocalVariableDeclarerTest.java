package falgout.jrepl.command.execute;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.command.parse.Statements;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class LocalVariableDeclarerTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public Statements statements = new Statements();
    public LocalVariableDeclarer declarer = new LocalVariableDeclarer();
    
}
