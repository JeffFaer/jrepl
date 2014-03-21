package falgout.jrepl.ui;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentModule;
import falgout.jrepl.command.Command;
import falgout.jrepl.command.CommandFactory;

public class TerminalRunner {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new EnvironmentModule());
        try (Environment env = injector.getInstance(Environment.class)) {
            CommandFactory f = injector.getInstance(CommandFactory.class);
            
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
                    Command<?> c = f.getCommand(env, input.toString());
                    c.execute(env);
                    input = new StringBuilder();
                }
                
                System.out.print(prompt);
            }
        }
    }
}
