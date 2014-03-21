package falgout.jrepl.ui;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentModule;

public class TerminalRunner {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new EnvironmentModule());
        Environment e = injector.getInstance(Environment.class);
        
        BufferedReader in = e.getInput();
        StringBuilder input = new StringBuilder();
        int braces = 0;
        String line;
        do {
            System.out.print("> ");
            line = in.readLine();
            if (line == null) {
                break;
            }

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
                e.execute(input.toString());
                input = new StringBuilder();
            }
        } while (!line.equals("=exit"));

    }
}
