package falgout.jrepl;

import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.common.base.Joiner;

public abstract class Import {
    private final String entireImport;
    private final boolean star;
    protected final String importedType;

    protected Import(Import other) {
        entireImport = other.entireImport;
        star = other.star;
        importedType = other.importedType;
    }

    protected Import(ImportDeclaration _import) {
        entireImport = _import.toString().trim();
        star = _import.isOnDemand();
        importedType = _import.getName().getFullyQualifiedName();
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
        public SingleImport(ImportDeclaration _import) {
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
        public StarImport(ImportDeclaration _import) {
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

    public static Import create(ImportDeclaration _import) {
        boolean _static = _import.isStatic();
        boolean star = _import.isOnDemand();
        
        Import baseImport = star ? new StarImport(_import) : new SingleImport(_import);
        return _static ? new StaticImport(baseImport) : new NormalImport(baseImport);
    }
}
