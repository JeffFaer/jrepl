package falgout.jrepl.command.execute.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.command.execute.BatchExecutor;

public abstract class CodeExecutor<T, R> extends BatchExecutor<SourceCode<? extends T>, R> {
    protected CodeExecutor() {}
    
    public R execute(GeneratedSourceCode<? extends T, ?> input) throws ExecutionException {
        return execute(input, Collections.EMPTY_LIST).get(0);
    }
    
    @SafeVarargs
    public final List<? extends R> execute(GeneratedSourceCode<? extends T, ?> first,
            GeneratedSourceCode<? extends T, ?>... rest) throws ExecutionException {
        return execute(first, Arrays.asList(rest));
    }
    
    public List<? extends R> execute(GeneratedSourceCode<? extends T, ?> first,
            Iterable<? extends GeneratedSourceCode<? extends T, ?>> rest) throws ExecutionException {
        List<SourceCode<? extends T>> code = new ArrayList<>();
        code.add(first);
        rest.forEach(code::add);
        return execute(first.getEnvironment(), code);
    }
}
