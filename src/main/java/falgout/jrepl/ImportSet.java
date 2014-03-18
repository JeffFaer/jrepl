package falgout.jrepl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;

class ImportSet extends ForwardingSet<Import> {
    private final Set<Import> imports = new LinkedHashSet<>();
    
    public ImportSet() {}
    
    public ImportSet(Collection<? extends Import> imports) {
        addAll(imports);
    }
    
    @Override
    protected Set<Import> delegate() {
        return imports;
    }
    
    @Override
    public boolean add(Import element) {
        boolean removed = false;
        Iterator<Import> itr = imports.iterator();
        while (itr.hasNext()) {
            Import i = itr.next();
            if (!removed && i.contains(element)) {
                return false;
            } else if (element.contains(i)) {
                removed = true;
                itr.remove();
            }
        }
        
        imports.add(element);

        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends Import> collection) {
        return standardAddAll(collection);
    }
}
