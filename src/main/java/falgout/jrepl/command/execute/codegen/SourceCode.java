package falgout.jrepl.command.execute.codegen;

import java.io.IOException;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.SimpleJavaFileObject;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Variable;

public abstract class SourceCode<T> extends SimpleJavaFileObject {
    private final String name;

    protected SourceCode(String name) {
        super(URI.create("string:///" + (name == null ? "" : name.replace('.', '/')) + Kind.SOURCE.extension),
                Kind.SOURCE);
        this.name = name;
    }

    public abstract T getTarget(Class<?> clazz);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract NestingKind getNestingKind();

    @Override
    public abstract Modifier getAccessLevel();

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return toString();
    }

    @Override
    public abstract String toString();

    private static class WrappedStatementSourceCode extends SourceCode<WrappedStatement> {
        private final WrappedStatement w;

        WrappedStatementSourceCode(WrappedStatement w) {
            super(null);
            this.w = w;
        }
        
        WrappedStatementSourceCode(Expression expression) {
            this(new WrappedStatement(expression));
        }
        
        WrappedStatementSourceCode(Statement statement) {
            this(new WrappedStatement(statement));
        }

        WrappedStatementSourceCode(Variable<?> variable) {
            this(new WrappedStatement(variable));
        }
        
        @Override
        public WrappedStatement getTarget(Class<?> clazz) {
            return w;
        }
        
        @Override
        public NestingKind getNestingKind() {
            return NestingKind.ANONYMOUS;
        }
        
        @Override
        public Modifier getAccessLevel() {
            return null;
        }
        
        @Override
        public String toString() {
            return w.toString();
        }
    }

    public static SourceCode<WrappedStatement> createReturnStatement(Variable<?> variable) {
        return new WrappedStatementSourceCode(variable);
    }

    public static SourceCode<WrappedStatement> createReturnStatement(Expression expression) {
        return new WrappedStatementSourceCode(expression);
    }

    public static SourceCode<WrappedStatement> from(Statement statement) {
        return new WrappedStatementSourceCode(statement);
    }
}
