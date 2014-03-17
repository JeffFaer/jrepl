package falgout.jrepl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.reflect.TypeToken;

public class VariableTest {
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
    
    public Variable<List<String>> var = new Variable<>(null, STRING_LIST);
    
    @Test
    public void variablesOfTheSameTypeCanBeAssigned() {
        Variable<List<String>> var2 = new Variable<List<String>>(Collections.EMPTY_LIST, STRING_LIST);
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
    }
    
    @Test
    public void variablesOfCovariantTypesCanBeAssigned() {
        Variable<ArrayList<String>> var2 = new Variable<>(new ArrayList<String>(), STRING_ARRAY_LIST);
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
        
        var.set((List<String>) null);
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
    }
    
    @Test
    public void genericTypesAreNotCovariant() {
        Variable<List<Integer>> var2 = new Variable<List<Integer>>(Collections.EMPTY_LIST, INT_LIST);
        
        assertFalse(var.set(var2));
        assertNotNull(var2.get());
        assertNull(var.get());
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
        assertNull(var.get());
    }
    
    @Test
    public void arraysAreCovariant() {
        Variable<CharSequence[]> var = new Variable<>(null, SEQUENCE_ARRAY);
        Variable<String[]> var2 = new Variable<>(new String[] { "1" }, STRING_ARRAY);
        
        assertTrue(var.set(var2));
        assertSame(var2.get(), var.get());
        
        var.set((CharSequence[]) null);
        
        assertFalse(var2.set(var));
        assertNotNull(var2.get());
    }
}
