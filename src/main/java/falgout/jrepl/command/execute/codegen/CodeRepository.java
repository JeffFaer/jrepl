package falgout.jrepl.command.execute.codegen;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

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
    
    private final ConcurrentMap<String, SourceCode<? extends T>> code = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, T> compiled = new ConcurrentHashMap<>();
    private final CodeCompiler<T> compiler;
    
    private final Set<SourceCode<? extends T>> allCode = Collections.unmodifiableSet(new SetView<>(code.values()));
    private final Set<T> allCompiled = Collections.unmodifiableSet(new SetView<>(compiled.values()));
    
    public CodeRepository(CodeCompiler<T> compiler) {
        this.compiler = compiler;
    }
    
    public CodeCompiler<T> getCompiler() {
        return compiler;
    }
    
    public boolean add(SourceCode<? extends T> code) {
        String name = code.getName();
        if (contains(name)) {
            return false;
        }
        
        SourceCode<? extends T> check = this.code.putIfAbsent(name, code);
        return check == null;
    }
    
    public SourceCode<? extends T> getCode(String name) {
        return code.get(name);
    }
    
    public boolean contains(String name) {
        return code.containsKey(name);
    }
    
    public Set<? extends SourceCode<? extends T>> getAllCode() {
        return allCode;
    }
    
    public Optional<? extends T> compile(Environment env, String name) throws ExecutionException {
        return contains(name) ? Optional.of(doCompile(env, name, code.get(name), false)) : Optional.empty();
    }
    
    public Optional<? extends T> compile(GeneratedSourceCode<? extends T, ?> code) throws ExecutionException {
        return compile(code.getEnvironment(), code);
    }
    
    public Optional<? extends T> compile(Environment env, SourceCode<? extends T> code) throws ExecutionException {
        return add(code) ? Optional.of(doCompile(env, code.getName(), code, true)) : Optional.empty();
    }
    
    private T doCompile(Environment env, String name, SourceCode<? extends T> code, boolean removeOnException)
            throws ExecutionException {
        if (compiled.containsKey(name)) {
            return compiled.get(name);
        }
        
        T ret;
        synchronized (code) {
            if (compiled.containsKey(name)) {
                return compiled.get(name);
            }
            
            try {
                ret = compiler.execute(env, code);
                compiled.put(name, ret);
            } catch (ExecutionException e) {
                if (removeOnException) {
                    this.code.remove(name, code);
                }
                throw e;
            }
        }
        return ret;
    }
    
    public T getCompiled(String name) {
        return compiled.get(name);
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
