package falgout.jrepl.command.parse;

import org.antlr.v4.runtime.ParserRuleContext;

import falgout.jrepl.parser.JavaParser;

public interface JavaParserRule<O extends ParserRuleContext> extends Parser<JavaParser, O> {}
