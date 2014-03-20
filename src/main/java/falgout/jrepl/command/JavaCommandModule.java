package falgout.jrepl.command;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class JavaCommandModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Key.get(new TypeLiteral<CommandFactory<?>>() {})).to(JavaCommandFactory.class);
    }
}
