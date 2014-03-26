package falgout.utils.reflection;

import static falgout.utils.reflection.Phase.ONE;
import static falgout.utils.reflection.Phase.THREE;
import static falgout.utils.reflection.Phase.TWO;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

public class PhaseTest {
    public static void varargs(String s, Object o, int i, long l, Integer i2, Long l2, Serializable... objects) {}
    
    public static void regular(String s, Object o, int i, long l, Integer i2, Long l2) {}
    
    private static final Parameterized.Method regularMethod;
    private static final Parameterized.Method varargsMethod;
    static {
        try {
            regularMethod = new Parameterized.Method(PhaseTest.class.getMethod("regular", String.class, Object.class,
                    int.class, long.class, Integer.class, Long.class));
            varargsMethod = new Parameterized.Method(PhaseTest.class.getMethod("varargs", String.class, Object.class,
                    int.class, long.class, Integer.class, Long.class, Serializable[].class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
    
    private static final Class<?>[] varArgsParams = new Class<?>[] { String.class, Object.class, int.class, long.class,
        Integer.class, Long.class, String.class };
    
    private static final Class<?>[] SUBTYPING = new Class<?>[] { String.class, String.class, byte.class, byte.class,
        Integer.class, Long.class };
    private static final Class<?>[] BOXING = new Class<?>[] { String.class, Object.class, int.class, long.class,
        int.class, Long.class };
    private static final Class<?>[] UNBOXING = new Class<?>[] { String.class, Object.class, int.class, Long.class,
        Integer.class, Long.class };
    
    private static final Class<?>[] CAST_REQUIRED = new Class<?>[] { Object.class, Object.class, int.class, long.class,
        Integer.class, Long.class };
    
    @Test
    public void PhaseOneTest() {
        assertTrue(ONE.isApplicable(regularMethod.getParameterTypes(), regularMethod));
        assertFalse(ONE.isApplicable(varArgsParams, varargsMethod));
        
        assertTrue(ONE.isApplicable(SUBTYPING, regularMethod));
        assertFalse(ONE.isApplicable(BOXING, regularMethod));
        assertFalse(ONE.isApplicable(UNBOXING, regularMethod));
        assertFalse(ONE.isApplicable(CAST_REQUIRED, regularMethod));
    }
    
    @Test
    public void PhaseTwoTest() {
        assertTrue(TWO.isApplicable(regularMethod.getParameterTypes(), regularMethod));
        assertFalse(TWO.isApplicable(varArgsParams, varargsMethod));
        
        assertTrue(TWO.isApplicable(SUBTYPING, regularMethod));
        assertTrue(TWO.isApplicable(BOXING, regularMethod));
        assertTrue(TWO.isApplicable(UNBOXING, regularMethod));
        assertFalse(TWO.isApplicable(CAST_REQUIRED, regularMethod));
    }
    
    @Test
    public void PhaseThreeTest() {
        assertTrue(THREE.isApplicable(varargsMethod.getParameterTypes(), varargsMethod));
        assertFalse(THREE.isApplicable(regularMethod.getParameterTypes(), regularMethod));
        
        assertTrue(THREE.isApplicable(SUBTYPING, varargsMethod));
        assertTrue(THREE.isApplicable(BOXING, varargsMethod));
        assertTrue(THREE.isApplicable(UNBOXING, varargsMethod));
        assertFalse(THREE.isApplicable(CAST_REQUIRED, varargsMethod));
        
        assertTrue(THREE.isApplicable(new Class<?>[] { String.class, Object.class, int.class, long.class,
                Integer.class, Long.class, Serializable.class, Serializable.class, Serializable.class }, varargsMethod));
        assertTrue(THREE.isApplicable(new Class<?>[] { String.class, Object.class, int.class, long.class,
                Integer.class, Long.class, String.class, Integer.class, Number.class }, varargsMethod));
        assertTrue(THREE.isApplicable(new Class<?>[] { String.class, Object.class, int.class, long.class,
                Integer.class, Long.class, String.class }, varargsMethod));
        assertTrue(THREE.isApplicable(new Class<?>[] { String.class, Object.class, int.class, long.class,
                Integer.class, Long.class, String[].class }, varargsMethod));
        assertFalse(THREE.isApplicable(new Class<?>[] { String.class, Object.class, int.class, long.class,
                Integer.class, Long.class, String.class, Object.class }, varargsMethod));
    }
}
