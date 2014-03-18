package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ImportTest {
    @Test
    public void canCreateMultipleImportsCorrectly() {
        assertEquals(2, Import.create("import java.lang.*;", "import java.math.*;").size());
    }
    
    @Test
    public void containsKeepsMostGeneralImport() {
        List<Import> imports = Import.create("import foo.bar.Class;", "import foo.*;", "import foo.bar.*;",
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
    public void normalImportOnlyReturnsSingleClass() {
        Import normal = Import.create("import java.lang.Object;").get(0);
        
        assertNull(normal.resolveClass("Integer"));
        assertNull(normal.resolveClassForField("fieldName"));
        assertNull(normal.resolveClassForMethod("methodName"));
        
        assertEquals("java.lang.Object", normal.resolveClass("Object"));
    }
    
    @Test
    public void starImportReturnsAnyClass() {
        Import star = Import.create("import java.lang.*;").get(0);
        
        assertNull(star.resolveClassForField("fieldName"));
        assertNull(star.resolveClassForMethod("methodName"));
        
        assertEquals("java.lang.Integer", star.resolveClass("Integer"));
        assertEquals("java.lang.Object", star.resolveClass("Object"));
    }
    
    @Test
    public void staticStarImportReturnsAny() {
        Import staticStar = Import.create("import static java.lang.Object.*;").get(0);
        
        assertEquals("java.lang.Object.Integer", staticStar.resolveClass("Integer"));
        assertEquals("java.lang.Object.Object", staticStar.resolveClass("Object"));
        assertEquals("java.lang.Object", staticStar.resolveClassForMethod("methodName"));
        assertEquals("java.lang.Object", staticStar.resolveClassForField("fieldName"));
    }
}
