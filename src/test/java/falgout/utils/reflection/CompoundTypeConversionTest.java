package falgout.utils.reflection;

import static falgout.utils.reflection.BasicTypeConversionTest.BoxingTest;
import static falgout.utils.reflection.BasicTypeConversionTest.UnboxingTest;
import static falgout.utils.reflection.BasicTypeConversionTest.WideningPrimitiveTest;
import static falgout.utils.reflection.BasicTypeConversionTest.WideningReferenceTest;
import static falgout.utils.reflection.BasicTypeConversionTest.nullCheckTo;
import static falgout.utils.reflection.BasicTypeConversionTest.voidCheckFrom;
import static falgout.utils.reflection.BasicTypeConversionTest.voidCheckTo;
import static falgout.utils.reflection.CompoundTypeConversion.METHOD_INVOCATION;
import static falgout.utils.reflection.CompoundTypeConversion.SUBTYPING;

import org.junit.Test;

public class CompoundTypeConversionTest {
    @Test
    public void MethodInvocationTest() {
        WideningPrimitiveTest(METHOD_INVOCATION);
        WideningReferenceTest(METHOD_INVOCATION);
        BoxingTest(METHOD_INVOCATION);
        UnboxingTest(METHOD_INVOCATION);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void MethodInvocationFailsWhenToIsNull() {
        nullCheckTo(METHOD_INVOCATION);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void MethodInvocationFailsWhenToIsVoid() {
        voidCheckTo(METHOD_INVOCATION);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void MethodInvocationFailsWhenFromIsVoid() {
        voidCheckFrom(METHOD_INVOCATION);
    }
    
    @Test
    public void SubtypingTest() {
        WideningPrimitiveTest(SUBTYPING);
        WideningReferenceTest(SUBTYPING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void SubtypingFailsWhenToIsNull() {
        nullCheckTo(SUBTYPING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void SubtypingFailsWhenToIsVoid() {
        voidCheckTo(SUBTYPING);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void SubtypingFailsWhenFromIsVoid() {
        voidCheckFrom(SUBTYPING);
    }
}
