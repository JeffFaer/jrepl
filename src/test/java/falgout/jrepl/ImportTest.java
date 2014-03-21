package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.jrepl.command.execute.Importer;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.guice.TestEnvironment;
import falgout.jrepl.guice.TestModule;

@RunWith(JukitoRunner.class)
@UseModules(TestModule.class)
public class ImportTest {
    @Inject @Rule public TestEnvironment env;

    public static List<Import> create(TestEnvironment env, String... imports) throws IOException {
        String source = String.join("", imports);
        ASTParser input = ASTParser.newParser(AST.JLS4);
        
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        
        input.setCompilerOptions(options);
        input.setSource(source.toCharArray());
        
        List<? extends CompilationUnit> ast = new ClassDeclaration().parse(input);
        List<Import> ret = Importer.PARSE.execute(env.getEnvironment(), ast).get();
        env.assertNoErrors();
        return ret;
    }
    
    @Test
    public void containsKeepsMostGeneralImport() throws IOException {
        List<Import> imports = create(env, "import foo.bar.Class;", "import foo.*;", "import foo.bar.*;",
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
        Import normal = create(env, "import java.lang.Object;").get(0);

        assertNull(normal.resolveClass("Integer"));
        assertNull(normal.resolveClassForField("fieldName"));
        assertNull(normal.resolveClassForMethod("methodName"));

        assertEquals("java.lang.Object", normal.resolveClass("Object"));
    }

    @Test
    public void starImportReturnsAnyClass() throws IOException {
        Import star = create(env, "import java.lang.*;").get(0);

        assertNull(star.resolveClassForField("fieldName"));
        assertNull(star.resolveClassForMethod("methodName"));

        assertEquals("java.lang.Integer", star.resolveClass("Integer"));
        assertEquals("java.lang.Object", star.resolveClass("Object"));
    }

    @Test
    public void staticStarImportReturnsAny() throws IOException {
        Import staticStar = create(env, "import static java.lang.Object.*;").get(0);

        assertEquals("java.lang.Object.Integer", staticStar.resolveClass("Integer"));
        assertEquals("java.lang.Object.Object", staticStar.resolveClass("Object"));
        assertEquals("java.lang.Object", staticStar.resolveClassForMethod("methodName"));
        assertEquals("java.lang.Object", staticStar.resolveClassForField("fieldName"));
    }
}
