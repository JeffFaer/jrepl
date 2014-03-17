package falgout.jrepl.antlr4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Various utility methods that are helpful for making decisions about trees.
 * I'm not well versed enough with the ANTLR4 API to know if these methods
 * already exist or not. If they do, this class will be removed.
 * 
 * @author jeffrey
 * 
 */
public class ParseTreeUtils {
    public static <P extends ParseTree> P getParent(ParseTree child, Class<P> clazz) {
        while ((child = child.getParent()) != null) {
            if (clazz.isInstance(child)) {
                return clazz.cast(child);
            }
        }
        
        return null;
    }
    
    public static <C extends ParseTree> List<? extends C> getChildren(ParseTree root, Class<C> clazz) {
        return getChildren(root, clazz, -1);
    }
    
    public static <C extends ParseTree> List<? extends C> getChildren(ParseTree root, Class<C> clazz, int depth) {
        if (depth == 0) {
            return Collections.EMPTY_LIST;
        }
        
        List<C> children = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            ParseTree child = root.getChild(i);
            if (clazz.isInstance(child)) {
                children.add(clazz.cast(child));
            }
            
            children.addAll(getChildren(child, clazz, depth - 1));
        }
        
        return children;
    }
}
