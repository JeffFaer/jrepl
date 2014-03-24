package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Window.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.reflect.TypeToken;

public class LocalVariableTest {
    private static final TypeToken<List<String>> STRING_LIST = new TypeToken<List<String>>() {
        private static final long serialVersionUID = -47114539417584131L;
    };
    private static final TypeToken<List<Integer>> INT_LIST = new TypeToken<List<Integer>>() {
        private static final long serialVersionUID = 7299409047954578940L;
    };
    private static final TypeToken<ArrayList<String>> STRING_ARRAY_LIST = new TypeToken<ArrayList<String>>() {
        private static final long serialVersionUID = 7902838928647569623L;
    };
    private static final TypeToken<String[]> STRING_ARRAY = TypeToken.of(String[].class);
    private static final TypeToken<CharSequence[]> SEQUENCE_ARRAY = TypeToken.of(CharSequence[].class);
    
    public LocalVariable<List<String>> var = new LocalVariable<>(STRING_LIST, "var", null);
    
    @Test
    public void variablesOfTheSameTypeCanBeAssigned() {
        LocalVariable<List<String>> var2 = new LocalVariable<List<String>>(STRING_LIST, "var2", Collections.EMPTY_LIST);
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
    }
    
    @Test
    public void variablesOfCovariantTypesCanBeAssigned() {
        LocalVariable<ArrayList<String>> var2 = new LocalVariable<>(STRING_ARRAY_LIST, "var2", new ArrayList<String>());
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
        
        var.set((List<String>) null);
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
    }
    
    @Test
    public void genericTypesAreNotCovariant() {
        LocalVariable<List<Integer>> var2 = new LocalVariable<List<Integer>>(INT_LIST, "var2", Collections.EMPTY_LIST);
        
        assertFalse(var.set(var2));
        assertNotNull(var2.get());
        assertNull(var.get());
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
        assertNull(var.get());
    }
    
    @Test
    public void arraysAreCovariant() {
        LocalVariable<CharSequence[]> var = new LocalVariable<>(SEQUENCE_ARRAY, "var", null);
        LocalVariable<String[]> var2 = new LocalVariable<>(STRING_ARRAY, "var2", new String[] { "1" });
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
        
        var.set((CharSequence[]) null);
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
    }
    
    @Test
    public void variablesCanBeUninitialized() {
        LocalVariable<Object> o = new LocalVariable<>(TypeToken.of(Object.class), "o");
        assertFalse(o.isInitialized());
        assertTrue(o.set((Object) null));
        assertTrue(o.isInitialized());
        assertTrue(o.set(new Object()));
        
        LocalVariable<Object> o2 = new LocalVariable<>(true, TypeToken.of(Object.class), "o2");
        assertFalse(o2.isInitialized());
        assertTrue(o2.set((Object) null));
        assertTrue(o2.isInitialized());
        assertFalse(o2.set(new Object()));
    }
    
    @Test
    public void variablesHaveDefaultValue() {
        LocalVariable<Integer> i = new LocalVariable<>(TypeToken.of(int.class), "i");
        assertEquals(0, (int) i.get());
        
        i.set((Integer) null);
        assertEquals(0, (int) i.get());
        
        LocalVariable<Integer> wrapped = new LocalVariable<>(TypeToken.of(Integer.class), "i2");
        assertNull(wrapped.get());
    }
    
    @Test
    public void rendersInnerTypesCorrectly() {
        LocalVariable<Type> v = new LocalVariable<>(TypeToken.of(Type.class), "v");
        assertEquals("java.awt.Window.Type v;", v.toString());
    }
}
