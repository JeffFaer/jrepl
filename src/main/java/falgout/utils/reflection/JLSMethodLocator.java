package falgout.utils.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

class JLSMethodLocator extends MethodLocator {
    @Override
    protected Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name, Class<?>... args)
        throws AmbiguousDeclarationException, NoSuchMethodException {
        return findParameterized(convertMethods(methods), clazz, name, args);
    }
    
    @Override
    protected Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name,
            Class<?>... args) throws NoSuchMethodException {
        return findParameterizeds(convertMethods(methods), clazz, name, args);
    }
    
    @Override
    protected <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
            Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException {
        return findParameterized(convertConstructors(constructors), clazz, "<init>", args);
    }
    
    @Override
    protected <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors,
            Class<T> clazz, Class<?>... args) throws NoSuchMethodException {
        return findParameterizeds(convertConstructors(constructors), clazz, "<init>", args);
    }
    
    private List<Parameterized.Method> convertMethods(Collection<? extends Method> methods) {
        List<Parameterized.Method> converted = new ArrayList<>(methods.size());
        for (Method m : methods) {
            converted.add(new Parameterized.Method(m));
        }
        return converted;
    }
    
    private <T> List<Parameterized.Constructor<T>> convertConstructors(Collection<? extends Constructor<T>> constructors) {
        List<Parameterized.Constructor<T>> converted = new ArrayList<>(constructors.size());
        for (Constructor<T> c : constructors) {
            converted.add(new Parameterized.Constructor<>(c));
        }
        return converted;
    }
    
    private <M extends AccessibleObject & GenericDeclaration & Member> M findParameterized(
            Collection<? extends Parameterized<M>> parameterizeds, Class<?> clazz, String name, Class<?>... args)
        throws AmbiguousDeclarationException, NoSuchMethodException {
        Set<M> found = findParameterizeds(parameterizeds, clazz, name, args);
        if (found.size() > 1) {
            throw new AmbiguousDeclarationException(found.toString());
        }
        return found.iterator().next();
    }
    
    private <M extends AccessibleObject & GenericDeclaration & Member> Set<M> findParameterizeds(
            Collection<? extends Parameterized<M>> parameterizeds, Class<?> clazz, String name, Class<?>... args)
        throws NoSuchMethodException {
        Set<Parameterized<M>> potentiallyApplicable = new LinkedHashSet<>();
        Predicate<Parameterized<?>> filter = new PotentiallyApplicable(name, args);
        for (Parameterized<M> p : parameterizeds) {
            if (filter.test(p)) {
                potentiallyApplicable.add(p);
            }
        }
        
        for (Phase phase : Phase.values()) {
            Set<Parameterized<M>> applicable = new LinkedHashSet<>();
            
            for (Parameterized<M> p : potentiallyApplicable) {
                if (phase.isApplicable(args, p)) {
                    applicable.add(p);
                }
            }
            
            if (!applicable.isEmpty()) {
                return getMostSpecific(applicable);
            }
        }
        
        throw new NoSuchMethodException(createNoSuchMethodMessage(clazz, name, args));
    }
    
    private <M extends AccessibleObject & GenericDeclaration & Member> Set<M> getMostSpecific(
            Collection<? extends Parameterized<M>> applicable) {
        Parameterized<M> max = Collections.max(applicable, MethodSpecificity.INSTANCE);
        Set<M> found = new LinkedHashSet<>();
        found.add(max.getMember());
        
        for (Parameterized<M> p : applicable) {
            if (MethodSpecificity.INSTANCE.compare(p, max) == 0) {
                found.add(p.getMember());
            }
        }
        
        return found;
    }
    
    private String createNoSuchMethodMessage(Class<?> clazz, String name, Class<?>... args) {
        StringBuilder b = new StringBuilder();
        b.append(toHumanReadableName(clazz)).append(".").append(name).append("(");
        for (int x = 0; x < args.length; x++) {
            if (x != 0) {
                b.append(", ");
            }
            b.append(toHumanReadableName(args[x]));
        }
        b.append(")");
        return b.toString();
    }
    
    private String toHumanReadableName(Class<?> clazz) {
        if (clazz == null) {
            return "null";
        }
        
        return clazz.isArray() ? toHumanReadableName(clazz.getComponentType()) + "[]" : clazz.getName();
    }
}
