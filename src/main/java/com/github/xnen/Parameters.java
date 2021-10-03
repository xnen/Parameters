package com.github.xnen;

import com.github.xnen.exception.ParameterException;
import com.github.xnen.handler.DefaultHelpHandler;
import com.github.xnen.impl.IHandler;
import com.github.xnen.param.ParamBuilder;
import com.github.xnen.param.Parameter;

import java.util.*;

/**
 * Basic Parameters Utility for easily registering and handling arguments passed to a Java application.
 * @author david
 */
public final class Parameters {

    private final List<Parameter> registered = new ArrayList<>();
    private Parameter defaultParam;

    private IHandler unhandled;
    private IHandler helpHandler;

    public Parameters(IHandler helpHandler) {
       this.helpHandler = helpHandler;
       init();
    }

    public Parameters(String jarName, String jarDescription, IHandler helpHandler) {
        if (helpHandler == null)
            this.helpHandler = new DefaultHelpHandler(this, System.out, jarName, jarDescription);
        this.init();
    }

    public Parameters(String jarName, String jarDescription) {
        this(jarName, jarDescription, null);
    }

    public void handleInvalidOptionsWith(IHandler unhandled) {
        this.unhandled = unhandled;
    }

    public void setDefaultParameter(Parameter parameter) {
        this.defaultParam = parameter;
    }

    private void init() {
        this.register(ParamBuilder.with()
                .identifier("--help", "-?")
                .description("Shows this help dialog.")
                .handler(this.helpHandler)
                .priority(Short.MAX_VALUE)
                .build());
    }

    public void register(Parameter parameter) {
        if (parameter == null)
            throw new RuntimeException("Parameter cannot be null!");

        if (paramIdExists(parameter)) {
            throw new RuntimeException("Could not register parameter, as a parameter that matches those identifiers already exist!");
        }

        this.registered.add(parameter);
        this.registered.sort((o1, o2) -> Short.compare(o2.getPriority(), o1.getPriority()));
    }

    private boolean paramIdExists(Parameter parameter) {
        for (String identifier : parameter.getIdentifiers()) {
            for (Parameter param : this.registered) {
                if (param.matches(identifier)) {
                    System.out.println(identifier + " matches with " + Arrays.toString(param.getIdentifiers()));
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Alias for process(String...)
     * Process args against registered parameters
     */
    public void accept(String... args) throws ParameterException {
        this.process(args);
    }

    /**
     * Process args against registered parameters.
     */
    public void process(String... args) throws ParameterException {
        String[] unhandledArgArray = new String[args.length];
        System.arraycopy(args, 0, unhandledArgArray, 0, args.length);

        Map<Parameter, String[]> paramArgs = new HashMap<>();
        String[] paramBuffer;

        for (int i = 0; i < args.length; i++) {
            for (Parameter parameter : this.registered) {
                if (parameter.matches(args[i])) {
                    int argCount;

                    if (!parameter.isInfinite()) {
                        argCount = parameter.getArgCount();
                    } else if (i + 1 < args.length) {
                        argCount = countValidInfiniteArgs(args, i);
                    } else {
                        argCount = 0;
                    }

                    if (i + argCount + 1 <= args.length) {
                        paramBuffer = new String[argCount];
                        for (int j = 0; j < argCount; j++) {
                            paramBuffer[j] = args[i + j + 1];
                            unhandledArgArray[i + j + 1] = null;
                        }

                        paramArgs.put(parameter, paramBuffer);
                    } else {
                        throw new ParameterException(0, "Parameter '" + parameter + "' consumes more args than are available.");
                    }

                    unhandledArgArray[i] = null;
                }
            }
        }

        String[] trimmed = trim(unhandledArgArray);

        if (this.defaultParam != null) {
            int expectedDefaultArgs = this.defaultParam.getArgCount() + 1;

            if (this.defaultParam.isInfinite() && trimmed.length > 0) {
                paramArgs.put(this.defaultParam, trimmed);
            } else {
                if (this.defaultParam.isRequired()) {
                    if (expectedDefaultArgs == trimmed.length) {
                        paramArgs.put(this.defaultParam, trimmed);
                    } else if (this.unhandled != null) {
                        if (trimmed.length > expectedDefaultArgs) {
                            String[] unhandledWithoutDefaults = new String[trimmed.length - expectedDefaultArgs];
                            System.arraycopy(trimmed, expectedDefaultArgs, unhandledWithoutDefaults, 0, trimmed.length - expectedDefaultArgs);
                            this.unhandled.handle(unhandledWithoutDefaults);
                            return;
                        } else {
                            /* TODO - Testing behavior by not throwing an exception here */ //throw new ParameterException(0, "Parameter '" + this.defaultParam + "' consumes more args than are available.");
                        }
                    }
                } else {
                    if (trimmed.length <= expectedDefaultArgs) {
                        if (trimmed.length > 0) {
                            paramArgs.put(this.defaultParam, trimmed);
                        }
                    } else if (this.unhandled != null) {
                        String[] unhandledWithoutDefaults = new String[trimmed.length - expectedDefaultArgs];
                        System.arraycopy(trimmed, expectedDefaultArgs, unhandledWithoutDefaults, 0, trimmed.length - expectedDefaultArgs);
                        this.unhandled.handle(unhandledWithoutDefaults);
                        return;
                    }
                }
            }
        } else if (this.unhandled != null && trimmed.length > 0) {
            this.unhandled.handle(trimmed);
            return;
        }

        List<Parameter> allParams = new ArrayList<>(this.registered);
        if (this.defaultParam != null)
            allParams.add(this.defaultParam);

        allParams.sort((o1, o2) -> Short.compare(o2.getPriority(), o1.getPriority()));

        for (Parameter parameter : allParams) {
            if (parameter.isRequired() && !paramArgs.containsKey(parameter)) {
                boolean flag = false;

                if (parameter.getRequiredClauses() != null) {
                    for (String s : parameter.getRequiredClauses()) {
                        for (Parameter param : paramArgs.keySet()) {
                            if (param.matches(s)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }

                // Allow --help to always be a required clause.
                for (Parameter param : paramArgs.keySet()) {
                    if (param.matches("--help")) {
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    throw new ParameterException(1, "Parameter '" + parameter + "' is required, but not present.");
                }
            }
        }

        for (Parameter parameter : allParams) {
            if (paramArgs.containsKey(parameter)) {
                if (!parameter.isValid(paramArgs.get(parameter))) {
                    throw new ParameterException(2, "Parameter '" + parameter + "' returned FALSE during validation.");
                }
            }
        }

        for (Parameter parameter : allParams) {
            if (paramArgs.containsKey(parameter)) {
                parameter.accept(paramArgs.get(parameter));
            }
        }
    }

    private String[] trim(String[] input) {
        return Arrays.stream(input).filter(Objects::nonNull).toArray(String[]::new);
    }

    public IHandler getHelpHandler() {
        return helpHandler;
    }

    private int countValidInfiniteArgs(String[] args, int fromIndex) {
        int j = 0;

        for (int i = fromIndex + 1; i < args.length; i++) {
            boolean flag = false;

            for (Parameter parameter1 : this.registered) {
                if (parameter1.matches(args[i])) {
                    return j;
                }
            }

            j++;
        }

        return j;
    }

    public Parameter getDefaultParameter() {
        return this.defaultParam;
    }

    public List<Parameter> getRegisteredParameters() {
        return this.registered;
    }
}
