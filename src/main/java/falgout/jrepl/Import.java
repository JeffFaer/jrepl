package falgout.jrepl;

import static falgout.jrepl.antlr4.ParseTreeUtils.joinText;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import com.google.common.base.Joiner;

import falgout.jrepl.command.parse.JavaLexer;
import falgout.jrepl.command.parse.JavaParser;
import falgout.jrepl.command.parse.JavaParser.ImportDeclarationContext;

public abstract class Import {
    private final String entireImport;
    private final boolean _static;
    private final boolean star;
    protected final String importedType;
    
    protected Import(Import other) {
        entireImport = other.entireImport;
        _static = other._static;
        star = other.star;
        importedType = other.importedType;
    }
    
    protected Import(ImportDeclarationContext _import) {
        importedType = joinText(_import.Identifier(), ".");
        _static = _import.STATIC() != null;
        star = _import.MULT() != null;
        
        StringBuilder b = new StringBuilder("import ");
        if (_static) {
            b.append("static");
        }
        b.append(importedType);
        if (star) {
            b.append(".*");
        }
        b.append(";");
        entireImport = b.toString();
    }
    
    public abstract String resolveClass(String className);
    
    public abstract String resolveClassForMethod(String methodName);
    
    public abstract String resolveClassForField(String fieldName);
    
    public boolean contains(Import other) {
        if (equals(other)) {
            return true;
        }
        
        if (!star) {
            return false;
        } else if (!other.star) {
            String trunc = other.importedType.substring(0, other.importedType.lastIndexOf('.'));
            return trunc.equals(importedType);
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entireImport == null) ? 0 : entireImport.hashCode());
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
        if (!(obj instanceof Import)) {
            return false;
        }
        Import other = (Import) obj;
        if (entireImport == null) {
            if (other.entireImport != null) {
                return false;
            }
        } else if (!entireImport.equals(other.entireImport)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return entireImport;
    }
    
    private static class SingleImport extends Import {
        public SingleImport(ImportDeclarationContext _import) {
            super(_import);
        }
        
        @Override
        public String resolveClass(String className) {
            return resolve(className, false);
        }
        
        @Override
        public String resolveClassForMethod(String methodName) {
            return resolve(methodName, true);
        }
        
        @Override
        public String resolveClassForField(String fieldName) {
            return resolve(fieldName, true);
        }
        
        private String resolve(String memberName, boolean truncate) {
            if (importedType.endsWith(memberName)) {
                int i = truncate ? importedType.length() - memberName.length() : importedType.length();
                return importedType.substring(0, i);
            }
            return null;
        }
    }
    
    private static class StarImport extends Import {
        public StarImport(ImportDeclarationContext _import) {
            super(_import);
        }
        
        @Override
        public String resolveClass(String className) {
            return Joiner.on('.').join(importedType, className);
        }
        
        @Override
        public String resolveClassForMethod(String methodName) {
            return importedType;
        }
        
        @Override
        public String resolveClassForField(String fieldName) {
            return importedType;
        }
    }
    
    private static class StaticImport extends Import {
        protected final Import delegate;
        
        public StaticImport(Import delegate) {
            super(delegate);
            this.delegate = delegate;
        }
        
        @Override
        public String resolveClass(String className) {
            return delegate.resolveClass(className);
        }
        
        @Override
        public String resolveClassForMethod(String methodName) {
            return delegate.resolveClassForMethod(methodName);
        }
        
        @Override
        public String resolveClassForField(String fieldName) {
            return delegate.resolveClassForField(fieldName);
        }
        
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }
        
        @Override
        public String toString() {
            return delegate.toString();
        }
    }
    
    private static class NormalImport extends StaticImport {
        public NormalImport(Import delegate) {
            super(delegate);
        }
        
        @Override
        public String resolveClassForField(String fieldName) {
            return null;
        }
        
        @Override
        public String resolveClassForMethod(String methodName) {
            return null;
        }
    }
    
    public static Import create(ImportDeclarationContext _import) {
        boolean _static = _import.STATIC() != null;
        boolean star = _import.MULT() != null;
        
        Import baseImport = star ? new StarImport(_import) : new SingleImport(_import);
        return _static ? new StaticImport(baseImport) : new NormalImport(baseImport);
    }
    
    public static List<Import> create(String... imports) {
        JavaLexer lex = new JavaLexer(new ANTLRInputStream(Joiner.on("").join(imports)));
        JavaParser parse = new JavaParser(new CommonTokenStream(lex));
        parse.setErrorHandler(new BailErrorStrategy());
        parse.getInterpreter().setPredictionMode(PredictionMode.SLL);
        
        int num = imports.length;
        List<Import> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add(create(parse.importDeclaration()));
        }
        
        return ret;
    }
}
