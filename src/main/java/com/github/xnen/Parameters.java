package com.github.xnen;

import com.github.xnen.exception.UnhandledParameterException;
import com.github.xnen.impl.IUnhandledHandler;
import com.github.xnen.param.IParameter;

import java.io.PrintStream;
import java.util.*;

public final class Parameters {
    private final List<IParameter> registeredParameters = new ArrayList<>();
    private IParameter defaultParameter;

    private final IUnhandledHandler unhandledHandler;
    private final String jarName, jarDescription;
    private String[] additionalHelpInfo;

    /**
     * Parameters library, used to easily register parameters for an application and translate them into an action.
     * To specify a default parameter (a parameter that requires no identifier), utilize 'setDefaultParameter'.
     *
     * @param jarName - The name of the application using this library
     * @param jarDescription - The description of the application using this library.
     * @param unhandledHandler - Must be implemented to handle unidentified parameters.
     */
    public Parameters(String jarName, String jarDescription, IUnhandledHandler unhandledHandler) {
        this.unhandledHandler = unhandledHandler;
        this.jarDescription = jarDescription;
        this.jarName = jarName;

        this.register(new IParameter() {
            public void handle(String[] args) {
                Parameters.this.printUsage(System.out, null);
                System.exit(0);
            }

            public String[] identifiers() {
                return new String[]{"--help"};
            }

            public String description() {
                return "Displays this help message.";
            }

            @Override
            public short priority() {
                return Short.MAX_VALUE;
            }
        });
    }

    public void setAdditionalHelpInfo(String[] additionalHelpInfo) {
        this.additionalHelpInfo = additionalHelpInfo;
    }

    public void setDefaultParameter(IParameter parameter) {
        this.defaultParameter = parameter;
    }

    /**
     * Register a new Parameter and order the array based on priority.
     */
    public void register(IParameter param) {
        this.registeredParameters.add(param);
        this.registeredParameters.sort(
                Comparator.comparingInt(IParameter::priority)
        );
        Collections.reverse(this.registeredParameters);
    }

    public void accept(String[] args) throws UnhandledParameterException {
        Map<IParameter, String[]> argBuckets = new HashMap<>();
        List<String[]> unhandledArgs = new ArrayList<>();
        List<String> unhandledBuffer = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            boolean flag0 = false;
            for (IParameter parameter : this.registeredParameters) {
                if (this.paramMatch(parameter, arg)) {
                    flag0 = true;

                    if (unhandledBuffer.size() > 0) {
                        unhandledArgs.add(unhandledBuffer.toArray(new String[0]));
                        unhandledBuffer.clear();
                    }

                    if (!argBuckets.containsKey(parameter)) {
                        if (parameter.argCount() == -1) {
                            List<String> paramBuffer = new ArrayList<>();
                            for (int j = 0; j < args.length - i; j++) {
                                String s = args[1 + j + i];

                                boolean flag = false;
                                for (IParameter parameter1 : this.registeredParameters) {
                                    if (this.paramMatch(parameter1, s)) {
                                        flag = true;
                                        break;
                                    }
                                }

                                if (!flag) {
                                    paramBuffer.add(s);
                                }
                            }
                            argBuckets.put(parameter, paramBuffer.toArray(new String[0]));
                        } else if (parameter.argCount() == 0) {
                            argBuckets.put(parameter, null);
                        } else {
                            if (i + parameter.argCount() + 1 > args.length) {
                                throw new UnhandledParameterException(0, "Parameter expects " + parameter.argCount() + " but there aren't that many arguments in the command!");
                            }

                            String[] paramArgs = new String[parameter.argCount()];

                            for (int j = 0; j < paramArgs.length; j++) {
                                paramArgs[j] = args[1 + j + i];
                            }

                            argBuckets.put(parameter, paramArgs);
                            i += parameter.argCount();
                        }
                    }
                }
            }
            if (!flag0) {
                unhandledBuffer.add(arg);
            }
        }

        boolean defaultShouldHandle = false;

        if (this.defaultParameter != null) {
            int defaultParamArgs = unhandledArgs.size() == 0 ? 0 : unhandledArgs.get(0).length;
            if (defaultParamArgs >= this.defaultParameter.argCount()) {
                if (this.defaultParameter.validate(unhandledArgs.get(0))) {
                    defaultShouldHandle = true;
                } else {
                    throw new UnhandledParameterException(3, "Default parameter was unable to validate input arguments.");
                }
            }
        }

        List<String> rawUnhandled = new ArrayList<>();

        // Ignore first parameter if it was handled by the default Parameter.
        for (int i = defaultShouldHandle ? 1 : 0; i < unhandledArgs.size(); i++) {
            rawUnhandled.addAll(Arrays.asList(unhandledArgs.get(i)));
        }

        // Stop the process if unhandled items exist, as this is likely an unintended user input.
        if (rawUnhandled.size() > 0) {
            this.unhandledHandler.handle(rawUnhandled);
            return;
        }

        // If all is well, continue handling.
        if (defaultShouldHandle) {
            this.defaultParameter.handle(unhandledArgs.get(0));
        }


        for (IParameter parameter : this.registeredParameters) {
            if (argBuckets.containsKey(parameter)) {
                String[] paramArgs = argBuckets.get(parameter);
                if (parameter.validate(paramArgs)) {
                    parameter.handle(paramArgs);
                } else {
                    throw new UnhandledParameterException(1, "Parameter " + parameter.identifiers()[0] + " failed to validate its arguments.");
                }
            } else if (parameter.required()) {
                throw new UnhandledParameterException(2, "Parameter " + parameter.identifiers()[0] + " is required, but not specified.");
            }
        }
    }

    private boolean paramMatch(IParameter parameter, String string) {
        for (String id : parameter.identifiers()) {
            if (parameter.caseSensitive() ? id.equals(string) : id.equalsIgnoreCase(string)) {
                return true;
            }
        }

        return false;
    }

    public void printUsage(PrintStream printStream, List<String> unhandledArgs) {
        if (unhandledArgs != null && unhandledArgs.size() > 0) {
            printStream.println(this.jarName + ": unrecognized option" + (unhandledArgs.size() > 1 ? "s" : "") + " " + Arrays.toString(unhandledArgs.toArray(new String[0])));
            printStream.println();
        }

        StringBuilder sb = new StringBuilder("Usage: " + this.jarName + " ");

        if (this.defaultParameter != null) {
            sb.append("[").append(this.defaultParameter.identifiers()[0].toUpperCase(Locale.ROOT)).append("] ");
        }

        for (IParameter parameter : this.registeredParameters) {
            if (parameter.required()) {
                sb.append(parameter.identifiers()[0]).append(" ");

                if (parameter.argCount() == -1) {
                    sb.append("[").append(parameter.argClarifiers()[0].toUpperCase(Locale.ROOT)).append("]... ");
                } else if (parameter.argClarifiers() != null) {
                    for (int i = 0; i < parameter.argClarifiers().length; i++) {
                        sb.append("[").append(parameter.argClarifiers()[i]).append("] ");
                    }
                }
            }
        }

        printStream.println(sb);
        printStream.println(this.jarDescription);
        printStream.println();

        for (IParameter parameter : this.registeredParameters) {
            StringBuilder paramLine = new StringBuilder("  ");
            for (String id : parameter.identifiers()) {
                if (paramLine.length() > 2) paramLine.append(", ");
                paramLine.append(id);
            }
            paramLine.append("\t");
            paramLine.append(parameter.description());
            printStream.println(paramLine);
        }

        if (this.additionalHelpInfo != null) {
            for (String s : this.additionalHelpInfo) {
                printStream.println(s);
            }
        }
    }

    public int getRegisteredParamCount() {
        return this.registeredParameters.size();
    }

}
