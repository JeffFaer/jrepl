package falgout.jrepl.command.execute.codegen;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import falgout.jrepl.reflection.NestedClass;

public class CodeGenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<CodeCompiler<NestedClass<?>>>() {}).toInstance(MemberCompiler.NESTED_CLASS_COMPILER);
    }
}
