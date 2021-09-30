package com.github.xnen.param;

/**
 * @author xnen
 *
 * Interface to specify how to handle parsing of a parameter.
 *
 * Valid optional overrides are : (defaults)
 *      validate() : true
 *      argCount() : 0
 *      argClarifiers() : null
 *
 *      caseSensitive() : false
 *      required() : false
 *      priority() : 0
 */
public interface IParameter {

    /**
     * @return Is the parameter case-sensitive?
     */
    default boolean caseSensitive() {
        return false;
    }

    /**
     * @return Is the parameter required?
     */
    default boolean required() {
        return false;
    }

    /**
     * @return The priority of this parameter's handling. The highest value is executed first.
     */
    default short priority() {
        return 0;
    }

    /**
     * @return The number of arguments this parameter should accept after its identifier
     * Noting that -1 is an infinite amount of arguments. (All arguments until a valid identifier WILL BE PASSED!)
     */
    default int argCount() {
        return 0;
    }

    /**
     * @return Whether the passed arguments to this parameter are satisfactory (correct type?)
     */
    default boolean validate(String[] args) {
        if (argCount() == 0) {
            return true;
        }

        throw new IllegalStateException("validate() must be overridden to validate arguments of this parameter! (argCount() returns >= 1)");
    }

    /**
     * Handle the passed arguments to this parameter.
     */
    void handle(String[] args);

    /**
     * @return Every valid identifier to invoke this parameter
     */
    String[] identifiers();

    /**
     * @return A clear description of each argument this Parameter accepts for a usage dialog. (I.e. 'FILE')
     */
    default String[] argClarifiers() {
        return null;
    }

    /**
     * @return A brief description for a usage dialog
     */
    String description();
}