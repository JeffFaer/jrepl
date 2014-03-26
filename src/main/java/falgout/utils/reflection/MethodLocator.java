package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * This class provides methods for finding a method or constructor in a given
 * class. You can either provide the actual arguments or the classes of the
 * actual arguments when using the methods in this class, but make sure to
 * provide the classes of the actual arguments in a {@code Class<?>[]} instead
 * of a {@code Object[]} or else the wrong overloaded method will be called. <br/>
 * <br/>
 * The methods in this class may throw one of two exceptions:
 * <ul>
 * <li>{@link AmbiguousDeclarationException}</li>
 * <ul>
 * <li>If more than one method is equally specific for the given name and
 * arguments.</li>
 * </ul>
 * <li>{@link NoSuchMethodException}</li>
 * <ul>
 * <li>If there is no method for the given name and arguments</li>
 * </ul>
 * </ul>
 * 
 * You can avoid the first by using a pluralized version of the method. I.E.
 * {@link #getMethods(Class, String, Object...) getMethods} instead of
 * {@link #getMethod(Class, String, Object...) getMethod}.
 * 
 * @author jeffrey
 * 
 */
public abstract class MethodLocator {
    public Method getMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
            NoSuchMethodException {
        return getMethod(clazz, name, ReflectionUtilities.getClasses(args));
    }
    
    public Method getMethod(Class<?> clazz, String name, Class<?>... args) throws AmbiguousDeclarationException,
            NoSuchMethodException {
        return getMethod(Arrays.asList(clazz.getMethods()), clazz, name, args);
    }
    
    public Set<Method> getMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
        return getMethods(clazz, name, ReflectionUtilities.getClasses(args));
    }
    
    public Set<Method> getMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
        return getMethods(Arrays.asList(clazz.getMethods()), clazz, name, args);
    }
    
    public Method getDeclaredMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
            NoSuchMethodException {
        return getDeclaredMethod(clazz, name, ReflectionUtilities.getClasses(args));
    }
    
    public Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... args)
            throws AmbiguousDeclarationException, NoSuchMethodException {
        return getMethod(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
    }
    
    public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
        return getDeclaredMethods(clazz, name, ReflectionUtilities.getClasses(args));
    }
    
    public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
        return getMethods(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
    }
    
    protected abstract Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name,
            Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
    
    protected abstract Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name,
            Class<?>... args) throws NoSuchMethodException;
    
    public <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) throws AmbiguousDeclarationException,
            NoSuchMethodException {
        return getConstructor(clazz, ReflectionUtilities.getClasses(args));
    }
    
    public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException,
            NoSuchMethodException {
        return getConstructor(ReflectionUtilities.getConstructors(clazz), clazz, args);
    }
    
    public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
        return getConstructors(clazz, ReflectionUtilities.getClasses(args));
    }
    
    public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Class<?>... args) throws NoSuchMethodException {
        return getConstructors(ReflectionUtilities.getConstructors(clazz), clazz, args);
    }
    
    public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Object... args)
            throws AmbiguousDeclarationException, NoSuchMethodException {
        return getDeclaredConstructor(clazz, ReflectionUtilities.getClasses(args));
    }
    
    public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args)
            throws AmbiguousDeclarationException, NoSuchMethodException {
        return getConstructor(ReflectionUtilities.getDeclaredConstructors(clazz), clazz, args);
    }
    
    public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
        return getDeclaredConstructors(clazz, ReflectionUtilities.getClasses(args));
    }
    
    public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Class<?>... args)
            throws NoSuchMethodException {
        return getConstructors(ReflectionUtilities.getDeclaredConstructors(clazz), clazz, args);
    }
    
    protected abstract <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors,
            Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
    
    protected abstract <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors,
            Class<T> clazz, Class<?>... args) throws NoSuchMethodException;
}
