package falgout.jrepl.command.parse;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingListIterator;

public class ClassDeclaration implements JavaParserRule<CompilationUnit> {
    @Override
    public List<? extends CompilationUnit> parse(ASTParser input) {
        input.setKind(ASTParser.K_COMPILATION_UNIT);
        List<CompilationUnit> l = Collections.singletonList((CompilationUnit) input.createAST(null));
        return new ForwardingList<CompilationUnit>() {
            @Override
            protected List<CompilationUnit> delegate() {
                return l;
            }
            
            @Override
            public Iterator<CompilationUnit> iterator() {
                return standardIterator();
            }

            @Override
            public ListIterator<CompilationUnit> listIterator() {
                return standardListIterator();
            }

            @Override
            public ListIterator<CompilationUnit> listIterator(int index) {
                ListIterator<CompilationUnit> delegate = standardListIterator(index);
                return new ForwardingListIterator<CompilationUnit>() {
                    @Override
                    protected ListIterator<CompilationUnit> delegate() {
                        return delegate;
                    }
                    
                    @Override
                    public void remove() {
                        // no op
                    }
                };
            }
        };
    }
}
