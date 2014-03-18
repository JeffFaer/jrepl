package falgout.jrepl.antlr4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import falgout.jrepl.command.parse.JavaParser;

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
    
    /**
     * @param parent The root node.
     * @param child The child to find.
     * @return {@code i} such that {@link ParseTree#getChild(int)
     *         parent.getChild(i)} is {@code child} or a parent of {@code child}
     *         , or {@code -1} if child is not a descendant of {@code parent}.
     */
    public static int getChildIndex(ParseTree parent, ParseTree child) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            ParseTree c = parent.getChild(i);
            if (c.equals(child) || getChildIndex(c, child) != -1) {
                return i;
            }
        }
        return -1;
    }
    
    public static String joinText(Iterable<? extends ParseTree> tree, String delim) {
        return Joiner.on(delim).join(Iterables.transform(tree, new Function<ParseTree, String>() {
            @Override
            public String apply(ParseTree input) {
                return input.getText();
            }
        }));
    }
    
    public static void openAndWait(ParseTree tree) {
        try {
            JDialog d = new TreeViewer(Arrays.asList(JavaParser.ruleNames), tree).open().get();
            Utils.waitForClose(d);
        } catch (InterruptedException | ExecutionException e) {
            throw new Error(e);
        }
    }
}
