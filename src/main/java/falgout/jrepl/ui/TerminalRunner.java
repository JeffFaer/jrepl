package falgout.jrepl.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentModule;
import falgout.jrepl.command.CommandFactory;
import falgout.jrepl.command.CommandModule;
import falgout.jrepl.command.ParsingException;

public class TerminalRunner {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new EnvironmentModule(), new CommandModule());
        try (Environment env = injector.getInstance(Environment.class)) {
            CommandFactory<Optional<? extends Collection<?>>> f = injector.getInstance(CommandFactory.class);
            
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
                        Optional<? extends Collection<?>> opt = f.execute(env, input.toString());
                        if (opt.isPresent()) {
                            for (Object o : opt.get()) {
                                System.out.println(o);
                            }
                        } else {
                            System.err.println("Did not execute");
                        }
                    } catch (ParsingException e) {
                        env.printStackTrace(e);
                    } catch (ExecutionException e) {
                        env.printStackTrace(e.getCause());
                    } catch (RuntimeException e) {
                        env.printStackTrace(e);
                    }
                    
                    input = new StringBuilder();
                }
                
                System.out.print(prompt);
            }
        }
    }
}
