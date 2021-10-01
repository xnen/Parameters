package com.github.xnen.handler;

import com.github.xnen.Parameters;
import com.github.xnen.impl.IHandler;
import com.github.xnen.param.Parameter;

import java.io.PrintStream;
import java.util.Locale;

public class DefaultHelpHandler implements IHandler {
    private final PrintStream printStream;
    private final Parameters parameters;
    private final String jarName;
    private final String jarDesc;

    public DefaultHelpHandler(Parameters parameters, PrintStream printStream, String jarName, String jarDesc) {
        this.printStream = printStream;
        this.parameters = parameters;
        this.jarDesc = jarDesc;
        this.jarName = jarName;
    }

    @Override
    public void handle(String... args) {
        this.showHelp();
    }

    public void showHelp() {
        StringBuilder sb = new StringBuilder("Usage: " + this.jarName + " ");

        if (parameters.getDefaultParameter() != null) {
            sb.append("[").append(parameters.getDefaultParameter().getMainIdentifier().toUpperCase(Locale.ROOT)).append("] ");
        }

        for (Parameter parameter : this.parameters.getRegisteredParameters()) {
            if (parameter.isRequired()) {
                sb.append(parameter.getMainIdentifier()).append(" ");

                if (parameter.isInfinite()) {
                    sb.append("<").append(parameter.getArgs().get(0)).append(">...");
                } else {
                    for (String s : parameter.getArgs()) {
                        sb.append("<").append(s).append("> ");
                    }
                }
            }
        }

        printStream.println(sb);
        printStream.println(this.jarDesc);
        printStream.println();

        for (Parameter parameter : this.parameters.getRegisteredParameters()) {
            StringBuilder paramLine = new StringBuilder("  ");
            for (String id : parameter.getIdentifiers()) {
                if (paramLine.length() > 2)
                    paramLine.append(", "); // Append comma only after an identifier has been appended.
                paramLine.append(id);
            }
            paramLine.append("\t");
            paramLine.append(parameter.getDescription());
            printStream.println(paramLine);
        }
    }
}
