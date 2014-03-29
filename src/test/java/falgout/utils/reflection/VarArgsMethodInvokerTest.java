package falgout.utils.reflection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class VarArgsMethodInvokerTest {
    public static Object[] varArgs(Object o1, Object... objects) {
        return objects;
    }
    
    public static int[] primitiveVarArgs(int i1, int... is) {
        return is;
    }
    
    private static final Class<?> CLAZZ = VarArgsMethodInvokerTest.class;
    private static final String NAME = "varArgs";
    private static final String NAME2 = "primitiveVarArgs";
    
    private static final MethodInvoker INVOKER = MethodInvoker.getDefault();
    
    @Test
    public void ZeroVarArgsAllowed() throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        assertEquals(0, ((Object[]) INVOKER.invokeStatic(CLAZZ, NAME, new Object())).length);
    }
    
    @Test
    public void ReusesSameArray() throws InvocationTargetException, IllegalAccessException, IllegalArgumentException,
        AmbiguousDeclarationException, NoSuchMethodException {
        Object[] args = { new Object(), new Object() };
        assertSame(args, INVOKER.invokeStatic(CLAZZ, NAME, new Object(), args));
    }
    
    @Test
    public void CreatesArray() throws InvocationTargetException, IllegalAccessException, IllegalArgumentException,
        AmbiguousDeclarationException, NoSuchMethodException {
        Object o1 = new Object();
        Object o2 = new Object();
        assertArrayEquals(new Object[] { o1, o2 }, (Object[]) INVOKER.invokeStatic(CLAZZ, NAME, new Object(), o1, o2));
    }
    
    @Test
    public void NullArrayAllowed() throws InvocationTargetException, IllegalAccessException, IllegalArgumentException,
        AmbiguousDeclarationException, NoSuchMethodException {
        assertNull(INVOKER.invokeStatic(CLAZZ, NAME, new Object(), null));
    }
    
    @Test
    public void PrimitiveZeroVarArgsAllowed() throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        assertEquals(0, ((int[]) INVOKER.invokeStatic(CLAZZ, NAME2, 5)).length);
    }
    
    @Test
    public void PrimitiveReusesSameArray() throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        int[] args = { 6, 7 };
        assertSame(args, INVOKER.invokeStatic(CLAZZ, NAME2, 5, args));
    }
    
    @Test
    public void PrimitiveCreatesArray() throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        assertArrayEquals(new int[] { 6, 7 }, (int[]) INVOKER.invokeStatic(CLAZZ, NAME2, 5, 6, 7));
    }
    
    @Test
    public void PrimitiveNullArrayAllowed() throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        assertNull(INVOKER.invokeStatic(CLAZZ, NAME2, 5, null));
    }
}
