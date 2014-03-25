package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import falgout.jrepl.guice.MethodExecutorFactory;
import falgout.jrepl.reflection.NestedClass;

public class CodeGenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<CodeCompiler<Class<?>>>() {}).toInstance(ClassCompiler.INSTANCE);
        install(new FactoryModuleBuilder().build(MethodExecutorFactory.class));
    }
    
    @Provides
    public CodeCompiler<NestedClass<?>> getNestedClassCompiler(CodeCompiler<Class<?>> compiler) {
        return new MemberCompiler<>(compiler);
    }
    
    @Provides
    public CodeCompiler<Method> getMethodCompiler(CodeCompiler<Class<?>> compiler) {
        return new MemberCompiler<>(compiler);
    }
}
