package falgout.utils.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class VarArgsMethodInvoker extends MethodInvoker {
    public VarArgsMethodInvoker(MethodLocator l) {
        super(l);
    }
    
    @Override
    protected Object invoke(Object instance, Method m, Object... args) throws InvocationTargetException,
    IllegalAccessException, IllegalArgumentException {
        try {
            return invoke(new Invokable.Method(m, instance), args);
        } catch (InstantiationException e) {
            // Method.invoke generated a Constructor.newInstance exception.
            // Something went wrong.
            throw new Error(e);
        }
    }
    
    @Override
    protected <T> T invoke(Constructor<T> cons, Object... args) throws InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        return invoke(new Invokable.Constructor<>(cons), args);
    }
    
    private <T> T invoke(Invokable<?, T> i, Object... args) throws InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        if (i.isVarArgs()) {
            args = repackageArguments(i.getParameterTypes(), args);
        }
        
        return i.invoke(args);
    }
    
    private Object[] repackageArguments(Class<?>[] parameterTypes, Object[] args) {
        Object[] newArgs = new Object[parameterTypes.length];
        int n = newArgs.length - 1;
        
        System.arraycopy(args, 0, newArgs, 0, n);
        
        int varArgsLength = args.length - n;
        Class<?> componentType = parameterTypes[n].getComponentType();
        
        Class<?> arr;
        Object varArgsArray;
        if (varArgsLength == 1
                && (args[n] == null || (arr = args[n].getClass()).isArray()
                && componentType.isAssignableFrom(arr.getComponentType()))) {
            // reuse array if it's of an appropriate type
            varArgsArray = args[n];
        } else if (componentType.isPrimitive()) {
            varArgsArray = unbox(componentType, args, n, varArgsLength);
        } else {
            varArgsArray = Array.newInstance(componentType, varArgsLength);
            System.arraycopy(args, n, varArgsArray, 0, varArgsLength);
        }
        
        newArgs[n] = varArgsArray;
        
        return newArgs;
    }
    
    private Object unbox(Class<?> componentType, Object[] args, int n, int varArgsLength) {
        if (componentType == boolean.class) {
            return unboxBoolean(args, n, varArgsLength);
        } else if (componentType == byte.class) {
            return unboxByte(args, n, varArgsLength);
        } else if (componentType == short.class) {
            return unboxShort(args, n, varArgsLength);
        } else if (componentType == char.class) {
            return unboxChar(args, n, varArgsLength);
        } else if (componentType == int.class) {
            return unboxInt(args, n, varArgsLength);
        } else if (componentType == long.class) {
            return unboxLong(args, n, varArgsLength);
        } else if (componentType == float.class) {
            return unboxFloat(args, n, varArgsLength);
        } else if (componentType == double.class) {
            return unboxDouble(args, n, varArgsLength);
        } else {
            throw new Error(componentType.toString() + " is unhandled.");
        }
    }
    
    private boolean[] unboxBoolean(Object[] args, int n, int varArgsLength) {
        boolean[] varArgs = new boolean[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (boolean) args[i];
        }
        return varArgs;
    }
    
    private byte[] unboxByte(Object[] args, int n, int varArgsLength) {
        byte[] varArgs = new byte[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (byte) args[i];
        }
        return varArgs;
    }
    
    private short[] unboxShort(Object[] args, int n, int varArgsLength) {
        short[] varArgs = new short[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (short) args[i];
        }
        return varArgs;
    }
    
    private char[] unboxChar(Object[] args, int n, int varArgsLength) {
        char[] varArgs = new char[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (char) args[i];
        }
        return varArgs;
    }
    
    private int[] unboxInt(Object[] args, int n, int varArgsLength) {
        int[] varArgs = new int[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (int) args[i];
        }
        return varArgs;
    }
    
    private long[] unboxLong(Object[] args, int n, int varArgsLength) {
        long[] varArgs = new long[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (long) args[i];
        }
        return varArgs;
    }
    
    private float[] unboxFloat(Object[] args, int n, int varArgsLength) {
        float[] varArgs = new float[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (float) args[i];
        }
        return varArgs;
    }
    
    private double[] unboxDouble(Object[] args, int n, int varArgsLength) {
        double[] varArgs = new double[varArgsLength];
        int c = 0;
        for (int i = n; i < args.length; i++) {
            varArgs[c++] = (double) args[i];
        }
        return varArgs;
    }
}
