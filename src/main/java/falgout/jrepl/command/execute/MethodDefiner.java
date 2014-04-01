package falgout.jrepl.command.execute;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.MethodSourceCode;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;
import falgout.jrepl.command.execute.codegen.OverloadedMethodSourceCode;
import falgout.jrepl.util.ThrowingBiFunction;

public class MethodDefiner extends RepositoryDefiner<MethodDeclaration, Method> {
    public static final MethodDefiner INSTANCE = new MethodDefiner();
    
    public MethodDefiner() {
        super(
                new ThrowingBiFunction<Environment, MethodDeclaration, NamedSourceCode<? extends Method>, ClassNotFoundException>() {
                    @Override
                    public NamedSourceCode<? extends Method> apply(Environment t, MethodDeclaration u)
                        throws ClassNotFoundException {
                        return apply(t, Collections.singleton(u)).get(0);
                    }
                    
                    @Override
                    public List<? extends NamedSourceCode<? extends Method>> apply(Environment t,
                            Collection<? extends MethodDeclaration> us) throws ClassNotFoundException {
                        List<NamedSourceCode<? extends Method>> code = new ArrayList<>(us.size());
                        Map<String, Integer> overloads = new LinkedHashMap<>();
                        MethodSourceCode.Builder builder = MethodSourceCode.builder();
                        for (MethodDeclaration decl : us) {
                            builder.initialize(MethodSourceCode.get(decl));
                            builder.addModifier(Modifier.STATIC);
                            
                            overloads.put(builder.getName(), code.size());
                            code.add(builder.build());
                        }
                        
                        // include all overloads with the method.
                        // static imports will be shadowed by the given method
                        // and if the method calls one of the overloads it will
                        // be a compile time error if we don't include the
                        // method in the same source file
                        CodeRepository<Method> repo = t.getMethodRepository();
                        overloads.forEach((name, i) -> {
                            if (repo.contains(name)) {
                                NamedSourceCode<? extends Method> target = code.get(i);
                                Collection<? extends NamedSourceCode<? extends Method>> others = repo.getCode(name)
                                        .get()
                                        .stream()
                                        .map(c -> {
                                            MethodSourceCode methodCode;
                                            
                                            while (c instanceof OverloadedMethodSourceCode) {
                                                c = ((OverloadedMethodSourceCode) c).getPrimary();
                                            }
                                            if (c instanceof MethodSourceCode) {
                                                methodCode = (MethodSourceCode) c;
                                            } else {
                                                throw new AssertionError(
                                                        "We've only put {Overloaded}MethodSourceCode in here,"
                                                                + " this shouldn't happen");
                                            }
                                            
                                            return MethodSourceCode.builder()
                                                    .initialize(methodCode)
                                                    .setModifiers(Modifier.PRIVATE)
                                                    .addModifier(Modifier.STATIC)
                                                    .build();
                                        })
                                        .collect(toList());
                                OverloadedMethodSourceCode delegate = OverloadedMethodSourceCode.builder(target, others)
                                        .build();
                                
                                code.set(i, delegate);
                            }
                        });
                        
                        return code;
                    }
                });
    }
    
    @Override
    protected CodeRepository<Method> getRepository(Environment env) {
        return env.getMethodRepository();
    }
}
