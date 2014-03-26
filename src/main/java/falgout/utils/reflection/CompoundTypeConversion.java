package falgout.utils.reflection;

import static falgout.utils.reflection.BasicTypeConversion.BOXING;
import static falgout.utils.reflection.BasicTypeConversion.IDENTITY;
import static falgout.utils.reflection.BasicTypeConversion.UNBOXING;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_PRIMITIVE;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_REFERENCE;

import java.util.Arrays;
import java.util.List;

public enum CompoundTypeConversion implements TypeConversion {
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.3"
     */
    METHOD_INVOCATION(IDENTITY, WIDENING_REFERENCE, WIDENING_PRIMITIVE, BOXING, UNBOXING),
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.10"
     */
    SUBTYPING(IDENTITY, WIDENING_REFERENCE, WIDENING_PRIMITIVE);
    
    private final List<TypeConversion> conversions;
    
    private CompoundTypeConversion(TypeConversion... conversions) {
        this.conversions = Arrays.asList(conversions);
    }
    
    @Override
    public boolean convert(Class<?> from, Class<?> to) {
        if (to == null) {
            throw new IllegalArgumentException("to == null");
        }
        for (TypeConversion t : conversions) {
            if (t.convert(from, to)) {
                return true;
            }
        }
        return false;
    }
}
