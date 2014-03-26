package falgout.jrepl.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentModule;
import falgout.jrepl.command.CommandFactory;
import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.ParsingException;
import falgout.jrepl.command.execute.ExecutorModule;
import falgout.jrepl.command.execute.codegen.CodeGenModule;
import falgout.jrepl.command.execute.codegen.GeneratedClass;
import falgout.jrepl.command.execute.codegen.GeneratedSourceCode;

public class TerminalRunner {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new EnvironmentModule(), new CommandModule(), new CodeGenModule(),
                new ExecutorModule());
        try (Environment env = injector.getInstance(Environment.class)) {
            CommandFactory<? extends Collection<? extends Optional<?>>> f = injector.getInstance(CommandFactory.class);
            
            String prompt = "java: ";
            BufferedReader in = env.getInput();
            StringBuilder input = new StringBuilder();
            int braces = 0;
            String line;
            System.out.print(prompt);
            while ((line = in.readLine()) != null && !line.equals("=exit")) {
                for (int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    if (ch == '{') {
                        braces++;
                    } else if (ch == '}') {
                        braces--;
                    }
                }
                
                if (input.length() != 0) {
                    input.append("\n");
                }
                input.append(line);
                
                if (braces == 0) {
                    try {
                        Collection<? extends Optional<?>> ret = f.execute(env, input.toString());
                        for (Optional<?> opt : ret) {
                            if (opt.isPresent()) {
                                System.out.println(opt.get());
                            }
                        }
                    } catch (ParsingException e) {
                        printStackTrace(env, e);
                    } catch (ExecutionException e) {
                        printStackTrace(env, e.getCause());
                    }
                    
                    input = new StringBuilder();
                }
                
                System.out.print(prompt);
            }
        }
    }
    
    public static void printStackTrace(Environment env, Throwable t) {
        t.printStackTrace();
        /*
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
            filterStackTrace(t);
            t.printStackTrace(env.getError());
        } else {
            while (t != null) {
                String message = t.getLocalizedMessage();
                if (!message.isEmpty()) {
                    env.getError().println(message);
                }
                t = t.getCause();
            }
        }*/
    }
    
    private static void filterStackTrace(Throwable t) {
        if (t == null) {
            return;
        }
        
        String pattern = GeneratedClass.PACKAGE + "." + GeneratedSourceCode.TEMPLATE;
        StackTraceElement[] st = t.getStackTrace();
        int i;
        for (i = 0; i < st.length; i++) {
            StackTraceElement ste = st[i];
            Pattern p = Pattern.compile(Pattern.quote(pattern) + "\\d+(.*)");
            Matcher m = p.matcher(ste.getClassName());
            if (m.matches()) {
                st[i] = new StackTraceElement(m.group(1), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
            } else {
                // TODO finish...
                break;
            }
        }
        t.setStackTrace(Arrays.copyOf(st, i));
        
        filterStackTrace(t.getCause());
        for (Throwable s : t.getSuppressed()) {
            filterStackTrace(s);
        }
    }
}
