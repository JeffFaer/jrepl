package falgout.jrepl.command.parse;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

public interface JavaParserRule<R extends ASTNode> extends Parser<ASTParser, List<? extends R>> {}
