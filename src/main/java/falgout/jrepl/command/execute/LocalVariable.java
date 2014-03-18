package falgout.jrepl.command.execute;

import static com.google.common.reflect.Types2.addArraysToType;
import static falgout.jrepl.reflection.Types.getType;
import static falgout.jrepl.reflection.Types.isFinal;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.antlr4.ParseTreeUtils;
import falgout.jrepl.command.parse.JavaParser.BlockStatementsContext;
import falgout.jrepl.command.parse.JavaParser.LocalVariableDeclarationContext;
import falgout.jrepl.command.parse.JavaParser.VariableDeclaratorContext;
import falgout.jrepl.command.parse.JavaParser.VariableDeclaratorRestContext;
import falgout.jrepl.command.parse.JavaParser.VariableInitializerContext;
import falgout.jrepl.reflection.ModifierException;

public class LocalVariable implements Executor<BlockStatementsContext> {
    @Override
    public boolean execute(Environment env, BlockStatementsContext input) throws IOException {
        for (LocalVariableDeclarationContext ctx : ParseTreeUtils.getChildren(input, LocalVariableDeclarationContext.class)) {
            boolean _final;
            try {
                _final = isFinal(ctx.variableModifier());
            } catch (ModifierException e) {
                env.getError().println(e.getMessage());
                return false;
            }
            
            TypeToken<?> baseType;
            try {
                baseType = getType(env.getImportClassLoader(), ctx.type());
            } catch (ClassNotFoundException e) {
                env.getError().println(e.getMessage());
                return false;
            }
            
            Map<String, Variable<?>> variables = new LinkedHashMap<>();
            for (VariableDeclaratorContext var : ctx.variableDeclarators().variableDeclarator()) {
                String name = var.Identifier().getText();
                
                if (env.containsVariable(name)) {
                    env.getError().printf("%s already exists.\n", name);
                    return false;
                }
                
                VariableDeclaratorRestContext rest = var.variableDeclaratorRest();
                int extraArrays = rest.L_BRACKET().size();
                TypeToken<?> varType = addArraysToType(baseType, extraArrays);
                
                VariableInitializerContext init = rest.variableInitializer();
                Object value = null;
                if (init != null) {
                    // TODO
                    // value = evaluate(init);
                }
                
                variables.put(name, new Variable<>(value, varType, _final));
            }
            
            if (!env.addVariables(variables)) {
                throw new Error("Something went horribly wrong");
            }
        }
        
        return true;
    }
    
}
