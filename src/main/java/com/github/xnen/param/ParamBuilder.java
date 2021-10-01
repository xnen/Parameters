package com.github.xnen.param;

import com.github.xnen.impl.IHandler;
import com.github.xnen.impl.IValidator;

public final class ParamBuilder implements IdentifyStep, DescriptionStep, HandleStep {

    private final Parameter param;

    private ParamBuilder() {
        this.param = new Parameter();
    }

    /**
     * Start to create a parameter
     */
    public static IdentifyStep with() {
        return new ParamBuilder();
    }

    /**
     * Return the built parameter
     */
    public Parameter build() {
        return this.param;
    }

    /**
     * Should the identifiers require case-sensitive inputs?
     */
    public ParamBuilder caseSensitive() {
        this.param.caseSensitive = true;
        return this;
    }

    /**
     * Make this parameter required, else an exception is thrown.
     */
    public ParamBuilder required() {
        this.param.required = true;
        return this;
    }

    /**
     * Make this parameter required. (Unless these identifiers are present)
     */
    public ParamBuilder requiredUnless(String... unless) {
        this.param.required = true;

        if (unless != null && unless.length > 0) {
            this.param.reqClauses = unless;
        }

        return this;
    }

    /**
     * What order should these arguments execute in? (Higher # => Earlier)
     */
    public ParamBuilder priority(short priority) {
        this.param.priority = priority;
        return this;
    }

    /**
     * How should the parameter validate that the arguments given to it are acceptable?
     */
    public ParamBuilder validator(IValidator validator) {
        this.param.validator = validator;
        return this;
    }

    /**
     * What text should this parameter look for to identify itself?
     * (i.e. "--test", "-t")
     */
    @Override
    public DescriptionStep identifier(String... identifiers) {
        this.param.identifiers = identifiers;
        return this;
    }

    /**
     * How should this parameter be described in a help menu?
     */
    @Override
    public HandleStep description(String desc) {
        this.param.description = desc;
        return this;
    }

    /**
     * What should this parameter do when identified?
     */
    @Override
    public ParamBuilder handler(IHandler handler) {
        this.param.handler = handler;
        return this;
    }

    /**
     * Allow the parameter to accept an argument.
     * The 'clarifier' is text shown to the user in the help dialog, to clarify what this parameter is accepting.
     */
    public ParamBuilder acceptArg(String clarifier) {
        this.param.addArg(clarifier);
        return this;
    }

    /**
     * Sets up parameter to accept an infinite amount of arguments after its identifier (until a future identifier is found)
     * ** NOTE ** This will CLEAR all previous arguments added to this parameter.
     */
    public ParamBuilder acceptsInfiniteArgs(String clarifier) {
        this.param.infinite = true;
        this.param.args.clear();
        this.param.args.add(clarifier);
        return this;
    }
}
