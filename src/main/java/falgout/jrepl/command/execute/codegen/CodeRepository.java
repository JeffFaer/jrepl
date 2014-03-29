package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import falgout.jrepl.Environment;

/**
 * Stores {@code NamedSourceCode} and its compiled equivalent by name.
 * 
 * @author jeffrey
 *
 * @param <T> The kind of {@code NamedSourceCode}.
 */
public class CodeRepository<T> {
    private static class SetView<E> extends AbstractSet<E> {
        private final Collection<E> values;
        
        public SetView(Collection<E> values) {
            this.values = values;
        }
        
        @Override
        public Iterator<E> iterator() {
            return values.iterator();
        }
        
        @Override
        public int size() {
            return values.size();
        }
    }
    
    private final Multimap<String, NamedSourceCode<? extends T>> code = LinkedHashMultimap.create();
    private final Map<NamedSourceCode<? extends T>, T> compiled = new LinkedHashMap<>();
    private final CodeCompiler<T> compiler;
    
    private final Set<NamedSourceCode<? extends T>> allCode = Collections.unmodifiableSet(new SetView<>(code.values()));
    private final Set<T> allCompiled = Collections.unmodifiableSet(new SetView<>(compiled.values()));
    
    public CodeRepository(CodeCompiler<T> compiler) {
        this.compiler = compiler;
    }
    
    public CodeCompiler<T> getCompiler() {
        return compiler;
    }
    
    @SafeVarargs
    public final boolean add(NamedSourceCode<? extends T> first, NamedSourceCode<? extends T>... rest) {
        return add(Lists.asList(first, rest));
    }
    
    /**
     * Adds code to this repository if there isn't already an entry under the
     * same name.
     * 
     * @param code The code to add to this repository
     * @return {@code true} if at least one of the elements in {@code code} was
     *         added.
     */
    public boolean add(Iterable<? extends NamedSourceCode<? extends T>> code) {
        int size = this.code.size();
        code.forEach(c -> {
            String name = c.getName();
            if (!this.code.get(name).contains(c)) {
                this.code.put(name, c);
            }
        });
        
        return size != this.code.size();
    }
    
    public boolean contains(String name) {
        return code.containsKey(name);
    }
    
    public Optional<? extends Collection<? extends NamedSourceCode<? extends T>>> getCode(String name) {
        return Optional.ofNullable(code.get(name)).map(Collections::unmodifiableCollection);
    }
    
    public Set<? extends NamedSourceCode<? extends T>> getAllCode() {
        return allCode;
    }
    
    public Collection<? extends T> compile(Environment env, String name) throws ExecutionException {
        return compile(env, new String[] { name }).get(name);
    }
    
    /**
     * Compiles the {@code NamedSourceCode} from this repository with the given
     * names.
     * 
     * @param env The {@code Environment} to compile in.
     * @param names The names of the {@code NamedSourceCode}.
     * @return A {@code List} in the same order as the given names where each
     *         element is an empty {@code Optional} if there is no
     *         {@code NamedSourceCode} with the given name or an
     *         {@code Optional} containing the compiled version.
     * @throws ExecutionException If there is an exception during compilation.
     */
    public Multimap<String, ? extends T> compile(Environment env, String... names) throws ExecutionException {
        Multimap<String, T> compiled = LinkedHashMultimap.create();
        Map<NamedSourceCode<? extends T>, Boolean> toCompile = new LinkedHashMap<>();
        for (String name : names) {
            if (contains(name)) {
                for (NamedSourceCode<? extends T> code : getCode(name).get()) {
                    toCompile.put(code, false);
                }
            }
        }
        
        Iterator<NamedSourceCode<? extends T>> i = toCompile.keySet().iterator();
        doCompile(env, toCompile).forEach(t -> compiled.put(i.next().getName(), t));
        return compiled;
    }
    
    public Optional<? extends T> compile(Environment env, NamedSourceCode<? extends T> code) throws ExecutionException {
        return compile(env, Collections.singleton(code)).map(l -> l.get(0));
    }
    
    @SafeVarargs
    public final Optional<? extends List<? extends T>> compile(Environment env, NamedSourceCode<? extends T>... code)
        throws ExecutionException {
        return compile(env, Arrays.asList(code));
    }
    
    /**
     * Attempts to add each {@code NamedSourceCode} into this repository. If any
     * one
     * piece of {@code NamedSourceCode} cannot be added (because there is
     * another
     * non-{@code ==} {@code NamedSourceCode} with the same name), this
     * method
     * will return an empty {@code Optional} and this repository will not have
     * been modified.
     * 
     * If all of the {@code NamedSourceCode} can be added, this method will
     * return an {@code Optional} containing a list of compiled versions in the
     * same order
     * as the {@code NamedSourceCode}. The {@code NamedSourceCode} and the
     * compiled
     * versions will have been added to this repository. If an
     * {@code ExecutionException} is thrown during compilation, any piece of
     * {@code NamedSourceCode} that was not already in this repository before
     * this
     * method was called will be removed.
     * 
     * @param env The {@code Environment} to compile in
     * @param code The {@code NamedSourceCode} to compile
     * @return An empty {@code Optional} if this repository has not been
     *         modified or an {@code Optional} with a list of all of the
     *         compiled members.
     * @throws ExecutionException If there's an exception during compilation
     */
    public Optional<? extends List<? extends T>> compile(Environment env,
            Iterable<? extends NamedSourceCode<? extends T>> code) throws ExecutionException {
        Map<NamedSourceCode<? extends T>, Boolean> add = new LinkedHashMap<>();
        for (NamedSourceCode<? extends T> c : code) {
            String name = c.getName();
            
            if (add.containsKey(c)) {
                return Optional.empty();
            } else if (contains(name)) {
                Collection<? extends NamedSourceCode<? extends T>> containedCode = getCode(name).get();
                if (containedCode.stream().anyMatch(contained -> c == contained)) {
                    add.put(c, false);
                } else if (containedCode.stream().anyMatch(c::equals)) {
                    return Optional.empty();
                } else {
                    add.put(c, true);
                }
            } else {
                add.put(c, true);
            }
        }
        
        return Optional.of(doCompile(env, add));
    }
    
    private List<? extends T> doCompile(Environment env, Map<NamedSourceCode<? extends T>, Boolean> shouldAdd)
        throws ExecutionException {
        List<T> ret = new ArrayList<>(shouldAdd.size());
        Map<NamedSourceCode<? extends T>, Integer> toCompile = new LinkedHashMap<>();
        shouldAdd.forEach((code, add) -> {
            if (!add && compiled.containsKey(code)) {
                ret.add(compiled.get(code)); // cached
            } else {
                toCompile.put(code, ret.size());
                ret.add(null); // placeholder
            }
        });
        
        Iterator<? extends T> compiled = compiler.execute(env, toCompile.keySet()).iterator();
        toCompile.forEach((code, index) -> {
            T t = compiled.next();
            this.compiled.put(code, t);
            ret.set(index, t); // replace placeholder
        });
        
        shouldAdd.forEach((code, add) -> {
            if (add) {
                this.code.put(code.getName(), code);
            }
        });
        return Collections.unmodifiableList(ret);
    }
    
    public boolean isCompiled(String name) {
        return code.get(name).stream().anyMatch(c -> compiled.containsKey(c));
    }
    
    public boolean isCompiled(NamedSourceCode<? extends T> code) {
        return compiled.containsKey(code);
    }
    
    public List<? extends Optional<? extends T>> getCompiled(String name) {
        return code.get(name)
                .stream()
                .map(c -> Optional.ofNullable(compiled.get(c)))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
    
    public Set<? extends T> getAllCompiled() {
        return allCompiled;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allCode == null) ? 0 : allCode.hashCode());
        result = prime * result + ((compiler == null) ? 0 : compiler.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CodeRepository)) {
            return false;
        }
        CodeRepository<?> other = (CodeRepository<?>) obj;
        if (allCode == null) {
            if (other.allCode != null) {
                return false;
            }
        } else if (!allCode.equals(other.allCode)) {
            return false;
        }
        if (compiler == null) {
            if (other.compiler != null) {
                return false;
            }
        } else if (!compiler.equals(other.compiler)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CodeRepository [compiler=");
        builder.append(compiler);
        builder.append(", allCode=");
        builder.append(allCode);
        builder.append("]");
        return builder.toString();
    }
}
