package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.Command;
import falgout.jrepl.command.JavaCommandFactory;
import falgout.jrepl.command.execute.Importer;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class ImportTest {
    @Inject @Rule public TestEnvironment env;
    public JavaCommandFactory<List<Import>> importParser = new JavaCommandFactory<>(new Pair<>(
            ClassDeclaration.INSTANCE, Importer.PARSE));
    
    public List<Import> create(String... imports) throws IOException {
        String source = String.join("", imports);
        Command<? extends Optional<? extends List<Import>>> c = importParser.getCommand(env.getEnvironment(), source);
        Optional<? extends List<Import>> opt = c.execute(env.getEnvironment());
        env.assertNoErrors();

        assertTrue(opt.isPresent());
        return opt.get();
    }

    @Test
    public void containsKeepsMostGeneralImport() throws IOException {
        List<Import> imports = create("import foo.bar.Class;", "import foo.*;", "import foo.bar.*;",
                "import static foo.bar.Class.*;", "import static foo.bar.Class.Member;",
                "import static foo.bar.Class.Member.*;", "import static foo.bar.Class.Member.field;");
        
        assertTrue(imports.get(0).contains(imports.get(0)));
        
        assertFalse(imports.get(1).contains(imports.get(2)));
        assertFalse(imports.get(2).contains(imports.get(1)));
        
        assertFalse(imports.get(0).contains(imports.get(2)));
        assertTrue(imports.get(2).contains(imports.get(0)));
        
        assertTrue(imports.get(3).contains(imports.get(4)));
        assertFalse(imports.get(4).contains(imports.get(3)));
        
        assertTrue(imports.get(5).contains(imports.get(6)));
        assertFalse(imports.get(6).contains(imports.get(5)));
        
        assertFalse(imports.get(3).contains(imports.get(6)));
        assertFalse(imports.get(6).contains(imports.get(3)));
    }
    
    @Test
    public void normalImportOnlyReturnsSingleClass() throws IOException {
        Import normal = create("import java.lang.Object;").get(0);
        
        assertNull(normal.resolveClass("Integer"));
        assertNull(normal.resolveClassForField("fieldName"));
        assertNull(normal.resolveClassForMethod("methodName"));
        
        assertEquals("java.lang.Object", normal.resolveClass("Object"));
    }
    
    @Test
    public void starImportReturnsAnyClass() throws IOException {
        Import star = create("import java.lang.*;").get(0);
        
        assertNull(star.resolveClassForField("fieldName"));
        assertNull(star.resolveClassForMethod("methodName"));
        
        assertEquals("java.lang.Integer", star.resolveClass("Integer"));
        assertEquals("java.lang.Object", star.resolveClass("Object"));
    }
    
    @Test
    public void staticStarImportReturnsAny() throws IOException {
        Import staticStar = create("import static java.lang.Object.*;").get(0);
        
        assertEquals("java.lang.Object$Integer", staticStar.resolveClass("Integer"));
        assertEquals("java.lang.Object$Object", staticStar.resolveClass("Object"));
        assertEquals("java.lang.Object", staticStar.resolveClassForMethod("methodName"));
        assertEquals("java.lang.Object", staticStar.resolveClassForField("fieldName"));
    }

    @Test
    public void resolveInnerTypesCorrectly() throws IOException {
        Import i = create("import java.awt.Window.Type;").get(0);
        assertEquals("java.awt.Window$Type", i.resolveClass("Type"));
    }
}
