package com.github.xnen.param;

import com.github.xnen.impl.IHandler;
import com.github.xnen.impl.IValidator;

import java.util.ArrayList;
import java.util.List;

public class Parameter {

    String[] identifiers;
    String[] reqClauses;
    String description;

    IValidator validator;
    IHandler handler;

    boolean caseSensitive;
    boolean required;

    boolean infinite;

    short priority;

    List<String> args = new ArrayList<>();

    Parameter() {}

    /**
     * Allow parameter to accept an argument. The 'argClarifier' is text that shows up in usage dialogs to clarify what this parameter accepts.
     */
    public void addArg(String argClarifier) {
        this.args.add(argClarifier);
    }

    /**
     * Description for usage dialogs.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Is this parameter required to be specified for the software to run?
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Tests if this parameter's validator accepts these args
     */
    public boolean isValid(String[] args) {
        if (this.validator == null) return true;
        return this.validator.validate(args);
    }

    /**
     * Use this parameter's handler to handle these args
     */
    public void accept(String[] args) {
        this.handler.handle(args);
    }

    /**
     * How early should this Parameter be handled in the list?
     * Higher => Earlier
     */
    public short getPriority() {
        return this.priority;
    }

    /**
     * Does this parameter match with this argument?
     * Uses 'caseSensitive' property for comparing
     */
    public boolean matches(String argIn) {
        for (String id : identifiers) {
            if (this.caseSensitive ? id.equals(argIn) : id.equalsIgnoreCase(argIn)) {
                return true;
            }
        }

        return false;
    }

    public int getArgCount() {
        if (this.args == null) return 0;
        return this.args.size();
    }

    public List<String> getArgs() {
        return args;
    }

    public boolean isInfinite() {
        return this.infinite;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(this.identifiers[0]);
        for (String s : this.args) sb.append(" <").append(s).append(">");
        return sb.toString();
    }

    public String[] getRequiredClauses() {
        return this.reqClauses;
    }

    public String getMainIdentifier() {
        return this.identifiers[0];
    }

    public String[] getIdentifiers() {
        return this.identifiers;
    }
}
