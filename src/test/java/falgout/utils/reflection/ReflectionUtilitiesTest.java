package falgout.utils.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.junit.Test;

public class ReflectionUtilitiesTest {
    @Test
    public void GetClassesTest() {
        Object[] args = { "hi", 5, null };
        Class<?>[] c = ReflectionUtilities.getClasses(args);
        
        assertSame(String.class, c[0]);
        assertSame(Integer.class, c[1]);
        assertSame(null, c[2]);
    }
    
    @Test
    public void getConstructorsTest() {
        Set<Constructor<String>> constructors = ReflectionUtilities.getConstructors(String.class);
        assertEquals(15, constructors.size());
        
        Set<Constructor<Integer>> ctors2 = ReflectionUtilities.getConstructors(Integer.class);
        assertEquals(2, ctors2.size());
    }
    
    @Test
    public void getDeclaredConstructorsTest() {
        Set<Constructor<String>> constructors = ReflectionUtilities.getDeclaredConstructors(String.class);
        assertEquals(16, constructors.size());
        
        Set<Constructor<Integer>> ctors2 = ReflectionUtilities.getDeclaredConstructors(Integer.class);
        assertEquals(2, ctors2.size());
    }
}
