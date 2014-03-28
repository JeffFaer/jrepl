package falgout.jrepl.command.execute.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class CodeRepositoryTest {
    @Inject @Rule public TestEnvironment env;
    @Inject public Environment e;
    public CodeRepository<Class<?>> repo = new CodeRepository<>(ClassCompiler.INSTANCE);
    
    public NamedSourceCode<Class<?>> foo1 = getCode("Foo", "public class Foo {}");
    public NamedSourceCode<Class<?>> foo2 = getCode("Foo", "public class Foo { static int foo; }");
    public NamedSourceCode<Class<?>> bar = getCode("Bar", "public class Bar {}");
    public NamedSourceCode<Class<?>> err = getCode("Err", "public class Err { ERROR }");
    
    public NamedSourceCode<Class<?>> getCode(String name, String code) {
        NamedSourceCode<Class<?>> clazz = mock(NamedSourceCode.class);
        when(clazz.getName()).thenReturn(name);
        when(clazz.toString()).thenReturn(code);
        return clazz;
    }
    
    @Test
    public void DoesNotEvictWhenAdding() {
        assertTrue(repo.add(foo1));
        assertFalse(repo.add(foo2));
        
        assertSame(foo1, repo.getCode("Foo").get());
    }
    
    @Test
    public void AddReturnsTrueIfAnyAreAdded() {
        assertTrue(repo.add(foo1));
        assertTrue(repo.add(foo2, bar));
        assertEquals(2, repo.getAllCode().size());
    }
    
    @Test
    public void CompileAddsUnaddedCode() throws ExecutionException {
        assertEquals(0, repo.getAllCode().size());
        repo.add(foo1);
        assertEquals(1, repo.getAllCode().size());
        repo.compile(e, foo1, bar);
        assertEquals(2, repo.getAllCode().size());
        assertEquals(2, repo.getAllCompiled().size());
    }
    
    @Test
    public void CompileDoesNotAddIfError() {
        try {
            repo.compile(e, foo1, bar, err);
            fail();
        } catch (ExecutionException e) {}
        assertEquals(0, repo.getAllCode().size());
    }
    
    @Test
    public void CanCompileByName() throws ExecutionException {
        repo.add(foo1);
        List<? extends Optional<? extends Class<?>>> compiled = repo.compile(e, "Foo", "Bar");
        assertTrue(compiled.get(0).isPresent());
        assertFalse(compiled.get(1).isPresent());
        
        assertEquals(1, repo.getAllCompiled().size());
    }
    
    @Test
    public void CompileDoesNotModifyIfDuplicateCode() throws ExecutionException {
        repo.add(foo1);
        
        Optional<? extends List<? extends Class<?>>> opt = repo.compile(e, foo2, bar);
        assertFalse(opt.isPresent());
        assertEquals(1, repo.getAllCode().size());
    }
    
    @Test
    public void CannotAddDuplicateCodeThroughCompile() throws ExecutionException {
        Optional<? extends List<? extends Class<?>>> opt = repo.compile(e, foo1, foo2);
        assertFalse(opt.isPresent());
        assertEquals(0, repo.getAllCode().size());
    }
}
