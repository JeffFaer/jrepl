package falgout.jrepl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;

import org.junit.Test;

import falgout.jrepl.reflection.GoogleTypes;

public class FieldVariableTest {
    public static int x = 0;
    public static Object y = null;
    private static final Field fieldX;
    private static final Field fieldY;
    static {
        try {
            fieldX = FieldVariableTest.class.getField("x");
            fieldY = FieldVariableTest.class.getField("y");
            
            fieldX.setAccessible(true);
            fieldY.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }
    
    @Test
    public void canSetPrimitiveFields() {
        FieldVariable<Integer> var = new FieldVariable<>(GoogleTypes.INT, fieldX);
        var.set(5);
        
        assertEquals(5, x);
    }
    
    @Test
    public void canSetReferenceFields() {
        FieldVariable<Object> var = new FieldVariable<>(GoogleTypes.OBJECT, fieldY);
        var.set(fieldX);
        
        assertSame(fieldX, y);
    }
}
