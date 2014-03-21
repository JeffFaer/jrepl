package falgout.jrepl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ImportDeclaration;

public abstract class Import {
    private final boolean _static;
    private final String _package;
    private final String type;
    private final List<String> innerTypes;
    private final boolean onDemand;

    protected Import(Import other) {
        _static = other._static;
        _package = other._package;
        type = other.type;
        innerTypes = other.innerTypes;
        onDemand = other.onDemand;
    }

    protected Import(boolean _static, String name, boolean onDemand) {
        this._static = _static;
        this.onDemand = onDemand;
        
        String[] parts = name.split("\\.");
        StringBuilder _package = new StringBuilder();
        String type = null;
        int i;
        for (i = 0; i < parts.length; i++) {
            String temp = parts[i];
            try {
                getClass().getClassLoader().loadClass(String.join(".", _package.toString(), temp));
                type = temp;
                i++;
                break;
            } catch (ClassNotFoundException e) {}

            if (_package.length() != 0) {
                _package.append(".");
            }
            _package.append(temp);
        }
        
        this._package = _package.toString();
        this.type = type == null ? null : type;
        innerTypes = Arrays.asList(parts).subList(i, parts.length);
    }

    public abstract String resolveClass(String className);

    public abstract String resolveClassForMethod(String methodName);

    public abstract String resolveClassForField(String fieldName);

    public boolean contains(Import other) {
        if (equals(other)) {
            return true;
        }

        if (!onDemand) {
            return false;
        } else if (!other.onDemand) {
            String base = getImportedName();
            String otherBase = other.getImportedName();

            // base = java.util (java.util.*)
            // otherBase = java.util.SomeClass
            if (base.equals(otherBase.substring(0, otherBase.lastIndexOf('.')))) {
                return true;
            }
        }

        return false;
    }
    
    protected String getImportedName() {
        return getImportedName(Optional.empty());
    }

    protected String getImportedType() {
        return getImportedType(Optional.empty());
    }
    
    protected String getImportedName(Optional<String> extraClass) {
        return join(".", extraClass);
    }

    protected String getImportedType(Optional<String> extraClass) {
        return join("$", extraClass);
    }

    private String join(String delim, Optional<String> extraClass) {
        StringBuilder name = new StringBuilder(_package);
        if (type != null) {
            name.append(".").append(type);
            for (String innerType : innerTypes) {
                name.append(delim);
                name.append(innerType);
            }
            
            extraClass.ifPresent(clazz -> name.append(delim).append(clazz));
        } else {
            extraClass.ifPresent(clazz -> name.append(".").append(clazz));
        }

        return name.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_package == null) ? 0 : _package.hashCode());
        result = prime * result + (_static ? 1231 : 1237);
        result = prime * result + ((innerTypes == null) ? 0 : innerTypes.hashCode());
        result = prime * result + (onDemand ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (_package == null) {
            if (other._package != null) {
                return false;
            }
        } else if (!_package.equals(other._package)) {
            return false;
        }
        if (_static != other._static) {
            return false;
        }
        if (innerTypes == null) {
            if (other.innerTypes != null) {
                return false;
            }
        } else if (!innerTypes.equals(other.innerTypes)) {
            return false;
        }
        if (onDemand != other.onDemand) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("import ");
        if (_static) {
            b.append("static ");
        }
        b.append(getImportedName());
        if (onDemand) {
            b.append(".*");
        }
        b.append(";");

        return b.toString();
    }

    private static class SingleImport extends Import {
        public SingleImport(boolean _static, String type, boolean onDemand) {
            super(_static, type, onDemand);
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
            String importedName = getImportedName();
            if (importedName.endsWith(memberName)) {
                String importedType = getImportedType();
                // chop the method/field name off
                if (truncate) {
                    int i = importedType.lastIndexOf('$');
                    importedType = i > 0 ? importedType.substring(0, i) : null;
                }
                return importedType;
            }
            return null;
        }
    }

    private static class StarImport extends Import {
        public StarImport(boolean _static, String type, boolean onDemand) {
            super(_static, type, onDemand);
        }

        @Override
        public String resolveClass(String className) {
            return getImportedType(Optional.of(className));
        }

        @Override
        public String resolveClassForMethod(String methodName) {
            return getImportedType();
        }

        @Override
        public String resolveClassForField(String fieldName) {
            return getImportedType();
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

    public static Import create(boolean _static, String name, boolean onDemand) {
        Import baseImport = onDemand ? new StarImport(_static, name, onDemand) : new SingleImport(_static, name,
                onDemand);
        return _static ? new StaticImport(baseImport) : new NormalImport(baseImport);
        
    }

    public static Import create(ImportDeclaration _import) {
        return create(_import.isStatic(), _import.getName().toString().trim(), _import.isOnDemand());
    }
}
