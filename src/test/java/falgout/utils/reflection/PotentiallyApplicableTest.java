package falgout.utils.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PotentiallyApplicableTest {
    public static void foo(Object one, Object two, Object... objects) {}
    
    public static void foo(Object one, Object two, Object three) {}
    
    private static final Parameterized.Method varargMethod;
    private static final Parameterized.Method regularMethod;
    private static final int arity = 3;
    static {
        try {
            varargMethod = new Parameterized.Method(PotentiallyApplicableTest.class.getMethod("foo", Object.class,
                    Object.class, Object[].class));
            regularMethod = new Parameterized.Method(PotentiallyApplicableTest.class.getMethod("foo", Object.class,
                    Object.class, Object.class));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
    
    @Test
    public void NameMustBeCorrect() {
        PotentiallyApplicable a = new PotentiallyApplicable("bogus", 3);
        assertFalse(a.test(varargMethod));
        assertFalse(a.test(regularMethod));
    }
    
    @Test
    public void RegularArityMustBeExact() {
        for (int x = 0; x < 10; x++) {
            PotentiallyApplicable a = new PotentiallyApplicable("foo", x);
            assertEquals(x == arity, a.test(regularMethod));
        }
    }
    
    @Test
    public void VariableArityMustBeGreaterThanNMinusOne() {
        for (int x = 0; x < 10; x++) {
            PotentiallyApplicable a = new PotentiallyApplicable("foo", x);
            assertEquals(x >= (arity - 1), a.test(varargMethod));
        }
    }
    
    @Test
    public void NamesWorkForConstructors() throws NoSuchMethodException {
        PotentiallyApplicable a = new PotentiallyApplicable("<init>", 0);
        assertTrue(a.test(new Parameterized.Constructor<>(PotentiallyApplicableTest.class.getConstructor())));
    }
}
