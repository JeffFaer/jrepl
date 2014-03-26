package falgout.utils.reflection;

import static falgout.utils.reflection.MethodSpecificity.INSTANCE;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class MethodSpecificityTest {
    public static void foo(Object o1, Object o2, Object o3) {}
    
    public static void foo(Serializable o1, Object o2, Object o3) {}
    
    public static void foo(Object o1, Object o2, Serializable o3) {}
    
    public static void foo(String o1, String o2, String o3) {}
    
    private static final Parameterized.Method method1;
    private static final Parameterized.Method method2A;
    private static final Parameterized.Method method2B;
    private static final Parameterized.Method method3;
    static {
        try {
            method1 = new Parameterized.Method(MethodSpecificityTest.class.getMethod("foo", Object.class, Object.class,
                    Object.class));
            method2A = new Parameterized.Method(MethodSpecificityTest.class.getMethod("foo", Serializable.class,
                    Object.class, Object.class));
            method2B = new Parameterized.Method(MethodSpecificityTest.class.getMethod("foo", Object.class,
                    Object.class, Serializable.class));
            method3 = new Parameterized.Method(MethodSpecificityTest.class.getMethod("foo", String.class, String.class,
                    String.class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
    
    public static void bar(Object o1, Object o2, Object... o3) {}
    
    public static void bar(Serializable o1, Object o2, Object... o3) {}
    
    public static void bar(Object o1, Object o2, Serializable o3, Object... o4) {}
    
    public static void bar(Object o1, Object o2, Serializable... o3) {}
    
    public static void bar(String o1, String o2, String o3, String o4, String... o5) {}
    
    private static final Parameterized.Method method4;
    private static final Parameterized.Method method5A;
    private static final Parameterized.Method method5B;
    private static final Parameterized.Method method5C;
    private static final Parameterized.Method method6;
    static {
        try {
            method4 = new Parameterized.Method(MethodSpecificityTest.class.getMethod("bar", Object.class, Object.class,
                    Object[].class));
            method5A = new Parameterized.Method(MethodSpecificityTest.class.getMethod("bar", Serializable.class,
                    Object.class, Object[].class));
            method5B = new Parameterized.Method(MethodSpecificityTest.class.getMethod("bar", Object.class,
                    Object.class, Serializable.class, Object[].class));
            method5C = new Parameterized.Method(MethodSpecificityTest.class.getMethod("bar", Object.class,
                    Object.class, Serializable[].class));
            method6 = new Parameterized.Method(MethodSpecificityTest.class.getMethod("bar", String.class, String.class,
                    String.class, String.class, String[].class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
    
    @Test
    public void NonVarargTest() {
        assertTrue(INSTANCE.compare(method1, method1) == 0);
        assertTrue(INSTANCE.compare(method1, method2A) < 0);
        assertTrue(INSTANCE.compare(method1, method2B) < 0);
        assertTrue(INSTANCE.compare(method1, method3) < 0);
        
        assertTrue(INSTANCE.compare(method2A, method1) > 0);
        assertTrue(INSTANCE.compare(method2A, method2A) == 0);
        assertTrue(INSTANCE.compare(method2A, method2B) == 0);
        assertTrue(INSTANCE.compare(method2A, method3) < 0);
        
        assertTrue(INSTANCE.compare(method2B, method1) > 0);
        assertTrue(INSTANCE.compare(method2B, method2A) == 0);
        assertTrue(INSTANCE.compare(method2B, method2B) == 0);
        assertTrue(INSTANCE.compare(method2B, method3) < 0);
        
        assertTrue(INSTANCE.compare(method3, method1) > 0);
        assertTrue(INSTANCE.compare(method3, method2A) > 0);
        assertTrue(INSTANCE.compare(method3, method2B) > 0);
        assertTrue(INSTANCE.compare(method3, method3) == 0);
    }
    
    @Test
    public void VarargTest() {
        assertTrue(INSTANCE.compare(method4, method4) == 0);
        assertTrue(INSTANCE.compare(method4, method5A) < 0);
        assertTrue(INSTANCE.compare(method4, method5B) < 0);
        assertTrue(INSTANCE.compare(method4, method5C) < 0);
        assertTrue(INSTANCE.compare(method4, method6) < 0);
        
        assertTrue(INSTANCE.compare(method5A, method4) > 0);
        assertTrue(INSTANCE.compare(method5A, method5A) == 0);
        assertTrue(INSTANCE.compare(method5A, method5B) == 0);
        assertTrue(INSTANCE.compare(method5A, method5C) == 0);
        assertTrue(INSTANCE.compare(method5A, method6) < 0);
        
        // whoa. Just because 5A == 5B and 5A == 5C doesn't mean 5B == 5C.
        // Interesting.
        assertTrue(INSTANCE.compare(method5B, method4) > 0);
        assertTrue(INSTANCE.compare(method5B, method5A) == 0);
        assertTrue(INSTANCE.compare(method5B, method5B) == 0);
        assertTrue(INSTANCE.compare(method5B, method5C) < 0);
        assertTrue(INSTANCE.compare(method5B, method6) < 0);
        
        assertTrue(INSTANCE.compare(method5C, method4) > 0);
        assertTrue(INSTANCE.compare(method5C, method5A) == 0);
        assertTrue(INSTANCE.compare(method5C, method5B) > 0);
        assertTrue(INSTANCE.compare(method5C, method5C) == 0);
        assertTrue(INSTANCE.compare(method5C, method6) < 0);
        
        assertTrue(INSTANCE.compare(method6, method4) > 0);
        assertTrue(INSTANCE.compare(method6, method5A) > 0);
        assertTrue(INSTANCE.compare(method6, method5B) > 0);
        assertTrue(INSTANCE.compare(method6, method5C) > 0);
        assertTrue(INSTANCE.compare(method6, method6) == 0);
    }
}
