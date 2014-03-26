package falgout.utils.reflection;

import static falgout.utils.reflection.BasicTypeConversion.BOXING;
import static falgout.utils.reflection.BasicTypeConversion.IDENTITY;
import static falgout.utils.reflection.BasicTypeConversion.UNBOXING;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_AND_NARROWING_PRIMITIVE;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_PRIMITIVE;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_REFERENCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BasicTypeConversionTest {
    @Test
    public void IdentityTest() {
        assertTrue(IDENTITY.convert(String.class, String.class));
        assertFalse(IDENTITY.convert(Object.class, String.class));
        assertFalse(IDENTITY.convert(String.class, Object.class));
        assertFalse(IDENTITY.convert(null, String.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void IdentityFailsWhenToIsNull() {
        nullCheckTo(IDENTITY);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void IdentityFailsWhenToIsVoid() {
        voidCheckTo(IDENTITY);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void IdentityFailsWhenFromIsVoid() {
        voidCheckFrom(IDENTITY);
    }
    
    public static void nullCheckTo(TypeConversion t) {
        t.convert(String.class, null);
    }
    
    public static void voidCheckTo(TypeConversion t) {
        t.convert(String.class, void.class);
    }
    
    public static void voidCheckFrom(TypeConversion t) {
        t.convert(void.class, String.class);
    }
    
    @Test
    public void WideningPrimitiveTest() {
        WideningPrimitiveTest(WIDENING_PRIMITIVE);
    }
    
    public static void WideningPrimitiveTest(TypeConversion t) {
        List<Class<?>> primitives = Arrays.<Class<?>> asList(byte.class, short.class, int.class, long.class,
                float.class, double.class);
        for (int x = 0; x < primitives.size(); x++) {
            assertTrue(t.convert(primitives.get(x), primitives.get(x)));
            for (int y = x + 1; y < primitives.size(); y++) {
                assertTrue(t.convert(primitives.get(x), primitives.get(y)));
            }
        }
        
        for (int x = primitives.indexOf(int.class); x < primitives.size(); x++) {
            assertTrue(t.convert(char.class, primitives.get(x)));
        }
        
        assertFalse(t.convert(null, int.class));
        assertFalse(t.convert(byte.class, char.class));
        assertFalse(t.convert(double.class, byte.class));
        assertFalse(t.convert(boolean.class, int.class));
        assertFalse(t.convert(char.class, short.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningPrimitiveFailsWhenToIsNull() {
        nullCheckTo(WIDENING_PRIMITIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningPrimitiveFailsWhenToIsVoid() {
        voidCheckTo(WIDENING_PRIMITIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningPrimitiveFailsWhenFromIsVoid() {
        voidCheckFrom(WIDENING_PRIMITIVE);
    }
    
    @Test
    public void WideningAndNarrowingPrimitiveTest() {
        assertTrue(WIDENING_AND_NARROWING_PRIMITIVE.convert(byte.class, char.class));
        assertFalse(WIDENING_AND_NARROWING_PRIMITIVE.convert(null, char.class));
        assertFalse(WIDENING_AND_NARROWING_PRIMITIVE.convert(short.class, char.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningAndNarrowingPrimitiveFailsWhenToIsNull() {
        nullCheckTo(WIDENING_AND_NARROWING_PRIMITIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningAndNarrowingPrimitiveFailsWhenToIsVoid() {
        voidCheckTo(WIDENING_AND_NARROWING_PRIMITIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningAndNarrowingPrimitiveFailsWhenFromIsVoid() {
        voidCheckFrom(WIDENING_AND_NARROWING_PRIMITIVE);
    }
    
    @Test
    public void WideningReferenceTest() {
        WideningReferenceTest(WIDENING_REFERENCE);
    }
    
    public static void WideningReferenceTest(TypeConversion t) {
        assertTrue(t.convert(String.class, Object.class));
        assertTrue(t.convert(null, Object.class));
        assertTrue(t.convert(null, String.class));
        assertTrue(t.convert(String.class, String.class));
        assertFalse(t.convert(Object.class, String.class));
        
        assertFalse(t.convert(null, int.class));
        
        assertTrue(t.convert(Integer[].class, Object[].class));
        assertTrue(t.convert(Object[].class, Object.class));
        assertTrue(t.convert(Object[].class, Cloneable.class));
        assertTrue(t.convert(Object[].class, Serializable.class));
        assertFalse(t.convert(Object[].class, Integer[].class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningReferenceFailsWhenToIsNull() {
        nullCheckTo(WIDENING_REFERENCE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningReferenceFailsWhenToIsVoid() {
        voidCheckTo(WIDENING_REFERENCE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void WideningReferenceFailsWhenFromIsVoid() {
        voidCheckFrom(WIDENING_REFERENCE);
    }
    
    @Test
    public void BoxingTest() {
        BoxingTest(BOXING);
    }
    
    public static void BoxingTest(TypeConversion t) {
        assertTrue(t.convert(boolean.class, Boolean.class));
        assertTrue(t.convert(byte.class, Byte.class));
        assertTrue(t.convert(short.class, Short.class));
        assertTrue(t.convert(char.class, Character.class));
        assertTrue(t.convert(int.class, Integer.class));
        assertTrue(t.convert(long.class, Long.class));
        assertTrue(t.convert(float.class, Float.class));
        assertTrue(t.convert(double.class, Double.class));
        
        assertTrue(t.convert(byte.class, Number.class));
        assertTrue(t.convert(double.class, Object.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BoxingFailsWhenToIsNull() {
        nullCheckTo(BOXING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BoxingFailsWhenToIsVoid() {
        voidCheckTo(BOXING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BoxingFailsWhenFromIsVoid() {
        voidCheckFrom(BOXING);
    }
    
    @Test
    public void UnboxingTest() {
        UnboxingTest(UNBOXING);
    }
    
    public static void UnboxingTest(TypeConversion t) {
        assertTrue(t.convert(Boolean.class, boolean.class));
        assertTrue(t.convert(Byte.class, byte.class));
        assertTrue(t.convert(Short.class, short.class));
        assertTrue(t.convert(Character.class, char.class));
        assertTrue(t.convert(Integer.class, int.class));
        assertTrue(t.convert(Long.class, long.class));
        assertTrue(t.convert(Float.class, float.class));
        assertTrue(t.convert(Double.class, double.class));
        
        assertTrue(t.convert(Byte.class, long.class));
        assertTrue(t.convert(Integer.class, double.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void UnboxingFailsWhenToIsNull() {
        nullCheckTo(UNBOXING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void UnboxingFailsWhenToIsVoid() {
        voidCheckTo(UNBOXING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void UnboxingFailsWhenFromIsVoid() {
        voidCheckFrom(UNBOXING);
    }
}
