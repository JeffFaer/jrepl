package falgout.utils.reflection;

import static falgout.utils.reflection.CompoundTypeConversion.SUBTYPING;

import java.util.Comparator;

enum MethodSpecificity implements Comparator<Parameterized<?>> {
    INSTANCE;
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2.5"
     * 
     * <pre>
     * if(compare(o1, o2) < 0) then o1 is less specific than o2.
     * 
     * if(compare(o1, o2) == 0) then o1 and o2 are equally specific.
     * 
     * if(compare(o1, o2) > 0) then o1 is more specific than o2.
     * 
     * if(o1.equals(o2)) then compare(o1, o2) == 0
     * </pre>
     */
    @Override
    public int compare(Parameterized<?> o1, Parameterized<?> o2) {
        if (o1.isVarArgs() && o2.isVarArgs()) {
            return compareVarArgs(o1, o2);
        } else if (!o1.isVarArgs() && !o2.isVarArgs()) {
            return compareNormal(o1, o2);
        } else {
            throw new IllegalArgumentException("o1 and o2 must come from the same Phase.");
        }
    }
    
    private int compareNormal(Parameterized<?> o1, Parameterized<?> o2) {
        int moreSpecific = 0;
        int lessSpecific = 0;
        for (int i = 0; i < o1.getParameterTypes().length; i++) {
            if (SUBTYPING.convert(o1.getParameterTypes()[i], o2.getParameterTypes()[i])) {
                moreSpecific++;
            }
            if (SUBTYPING.convert(o2.getParameterTypes()[i], o1.getParameterTypes()[i])) {
                lessSpecific++;
            }
        }
        
        return determineResult(moreSpecific, lessSpecific, o1.getParameterTypes().length);
    }
    
    private int determineResult(int moreSpecific, int lessSpecific, int maxArity) {
        if (moreSpecific == lessSpecific) {
            return 0;
        } else if (moreSpecific == maxArity) {
            return 1;
        } else if (lessSpecific == maxArity) {
            return -1;
        } else {
            return 0;
        }
    }
    
    private int compareVarArgs(Parameterized<?> o1, Parameterized<?> o2) {
        int max1 = o1.getParameterTypes().length - 1;
        int max2 = o2.getParameterTypes().length - 1;
        
        Class<?>[] c1 = o1.getParameterTypes();
        Class<?>[] c2 = o2.getParameterTypes();
        c1[max1] = c1[max1].getComponentType();
        c2[max2] = c2[max2].getComponentType();
        
        int moreSpecific = 0;
        int lessSpecific = 0;
        
        int max = Math.max(max1, max2) + 1;
        int i1 = 0;
        int i2 = 0;
        for (int i = 0; i < max; i++) {
            if (SUBTYPING.convert(c1[i1], c2[i2])) {
                moreSpecific++;
            }
            if (SUBTYPING.convert(c2[i2], c1[i1])) {
                lessSpecific++;
            }
            
            if (i1 < max1) {
                i1++;
            }
            if (i2 < max2) {
                i2++;
            }
        }
        
        return determineResult(moreSpecific, lessSpecific, max);
    }
}
