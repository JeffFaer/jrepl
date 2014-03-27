package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.toList;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Lists;

import falgout.jrepl.Environment;

/**
 * Stores {@code SourceCode} and its compiled equivalent by name.
 * 
 * @author jeffrey
 *
 * @param <T> The kind of {@code SourceCode}.
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
    
    private final Map<String, SourceCode<? extends T>> code = new LinkedHashMap<>();
    private final Map<String, T> compiled = new LinkedHashMap<>();
    private final CodeCompiler<T> compiler;
    
    private final Set<SourceCode<? extends T>> allCode = Collections.unmodifiableSet(new SetView<>(code.values()));
    private final Set<T> allCompiled = Collections.unmodifiableSet(new SetView<>(compiled.values()));
    
    public CodeRepository(CodeCompiler<T> compiler) {
        this.compiler = compiler;
    }
    
    public CodeCompiler<T> getCompiler() {
        return compiler;
    }
    
    @SafeVarargs
    public final boolean add(SourceCode<? extends T> first, SourceCode<? extends T>... rest) {
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
    public boolean add(Iterable<? extends SourceCode<? extends T>> code) {
        int size = this.code.size();
        code.forEach(c -> {
            String name = c.getName();
            if (!contains(name)) {
                this.code.put(name, c);
            }
        });
        
        return size != this.code.size();
    }
    
    public Optional<? extends SourceCode<? extends T>> getCode(String name) {
        return Optional.ofNullable(code.get(name));
    }
    
    public boolean contains(String name) {
        return code.containsKey(name);
    }
    
    public Set<? extends SourceCode<? extends T>> getAllCode() {
        return allCode;
    }
    
    public Optional<? extends T> compile(Environment env, String name) throws ExecutionException {
        return compile(env, new String[] { name }).get(0);
    }
    
    /**
     * Compiles the {@code SourceCode} from this repository with the given
     * names.
     * 
     * @param env The {@code Environment} to compile in.
     * @param names The names of the {@code SourceCode}.
     * @return A {@code List} in the same order as the given names where each
     *         element is an empty {@code Optional} if there is no
     *         {@code SourceCode} with the given name or an {@code Optional}
     *         containing the compiled version.
     * @throws ExecutionException If there is an exception during compilation.
     */
    public List<Optional<? extends T>> compile(Environment env, String... names) throws ExecutionException {
        Map<SourceCode<? extends T>, Boolean> toCompile = new LinkedHashMap<>();
        for (String name : names) {
            if (contains(name)) {
                toCompile.put(getCode(name).get(), false);
            } else {
                toCompile.put(null, null);
            }
        }
        
        return doCompile(env, toCompile);
    }
    
    public Optional<? extends T> compile(GeneratedSourceCode<? extends T, ?> code) throws ExecutionException {
        return getFirst(compile(code, Collections.EMPTY_LIST));
    }
    
    private Optional<? extends T> getFirst(Optional<? extends List<? extends T>> opt) {
        return opt.isPresent() ? Optional.of(opt.get().get(0)) : Optional.empty();
    }
    
    @SafeVarargs
    public final Optional<? extends List<? extends T>> compile(GeneratedSourceCode<? extends T, ?> first,
            GeneratedSourceCode<? extends T, ?>... rest) throws ExecutionException {
        return compile(first, Arrays.asList(rest));
    }
    
    public Optional<? extends List<? extends T>> compile(GeneratedSourceCode<? extends T, ?> first,
            Iterable<? extends GeneratedSourceCode<? extends T, ?>> rest) throws ExecutionException {
        List<SourceCode<? extends T>> code = new ArrayList<>();
        code.add(first);
        rest.forEach(code::add);
        return compile(first.getEnvironment(), code);
    }
    
    public Optional<? extends T> compile(Environment env, SourceCode<? extends T> code) throws ExecutionException {
        return getFirst(compile(env, Collections.singleton(code)));
    }
    
    @SafeVarargs
    public final Optional<? extends List<? extends T>> compile(Environment env, SourceCode<? extends T>... code)
            throws ExecutionException {
        return compile(env, Arrays.asList(code));
    }
    
    /**
     * Attempts to add each {@code SourceCode} into this repository. If any one
     * piece of {@code SourceCode} cannot be added (because there is another
     * non-{@code equal} {@code SourceCode} with the same name), this method
     * will return an empty {@code Optional} and this repository will not have
     * been modified.
     * 
     * If all of the {@code SourceCode} can be added, this method will return an
     * {@code Optional} containing a list of compiled versions in the same order
     * as the {@code SourceCode}. The {@code SourceCode} and the compiled
     * versions will have been added to this repository. If an
     * {@code ExecutionException} is thrown during compilation, any piece of
     * {@code SourceCode} that was not already in this repository before this
     * method was called will be removed.
     * 
     * @param env The {@code Environment} to compile in
     * @param code The {@code SourceCode} to compile
     * @return An empty {@code Optional} if this repository has not been
     *         modified or an {@code Optional} with a list of all of the
     *         compiled members.
     * @throws ExecutionException If there's an exception during compilation
     */
    public Optional<? extends List<? extends T>> compile(Environment env,
            Iterable<? extends SourceCode<? extends T>> code) throws ExecutionException {
        Map<SourceCode<? extends T>, Boolean> add = new LinkedHashMap<>();
        Set<String> names = new LinkedHashSet<>();
        for (SourceCode<? extends T> c : code) {
            String name = c.getName();
            
            if (names.contains(name)) {
                return Optional.empty();
            } else if (contains(name)) {
                if (getCode(name).get().equals(c)) {
                    add.put(c, false);
                } else {
                    return Optional.empty();
                }
            } else {
                names.add(name);
                add.put(c, true);
            }
        }
        
        return Optional.of(doCompile(env, add).stream().map(opt -> opt.get()).collect(toList()));
    }
    
    private List<Optional<? extends T>> doCompile(Environment env, Map<SourceCode<? extends T>, Boolean> shouldAdd)
            throws ExecutionException {
        List<Optional<? extends T>> ret = new ArrayList<>(shouldAdd.size());
        Map<SourceCode<? extends T>, Integer> toCompile = new LinkedHashMap<>();
        shouldAdd.forEach((code, add) -> {
            if (add == null) {
                ret.add(Optional.empty());
            } else {
                String name = code.getName();
                if (!add && compiled.containsKey(name)) {
                    ret.add(Optional.of(compiled.get(name))); // cached
                } else {
                    toCompile.put(code, ret.size());
                    ret.add(null); // placeholder
                }
            }
        });
        
        Queue<? extends T> compiled = new LinkedList<>(compiler.execute(env, toCompile.keySet()));
        toCompile.forEach((code, index) -> {
            T t = compiled.poll();
            this.compiled.put(code.getName(), t);
            ret.set(index, Optional.of(t)); // replace placeholder
        });
        
        shouldAdd.forEach((code, add) -> {
            if (add != null && add) {
                this.code.put(code.getName(), code);
            }
        });
        return ret;
        
    }
    
    public boolean isCompiled(String name) {
        return compiled.containsKey(name);
    }
    
    public Optional<? extends T> getCompiled(String name) {
        return Optional.ofNullable(compiled.get(name));
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
