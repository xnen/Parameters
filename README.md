# Parameters
Basic parameter handling library for Java console applications

**Severe WIP**, bugs are guaranteed, features are likely to completely not work.
Not much tested yet, including building.

### Getting Started:
1. Create an IUnhandledHandler, this can be done easily with a lambda

```
IUnhandledHandler handler = new IUnhandledHandler() {
    @Override public void handle(List<String> unhandledArguments) {
        // Handle unhandled arguments here.
    }
}
```
2. Create a Parameters object in your class ``Parameters p = new Parameters(APP_NAME, APP_DESC, handler);``
3. Register your parameters using `Parameter::register()`

Example:
```
    this.parameters.register(new IParameter() {
        @Override
        public void handle(String[] args) {
            // handled
        }
        
        @Override
        public String[] identifiers() {
            return new String[] { "--test" };
        }
        
        @Override
        public String description() {
            return "Test!";
        }
    });
```

4. Pass all incoming arguments from `public static void main(String[] args)` to the parameters using ``Parameters::accept()``

### Note-worthy:

There are a few optional overrides in each IParameter:
* caseSensitive(), defaults FALSE. Should matching any identifier be case-sensitive?
* required(), defaults FALSE
* priority(), defaults 0. The higher number, the earlier it is handled.
* argCount(), defaults 0. The parameter will ALWAYS consume this many arguments.
    * An argCount() of -1 will be infinite until a future parameter identifier is found. Parameter identifiers in quote blocks will not be used.
    * Any argCount() override not equal to ZERO will require a few overrides:
        * validate() - Must validate the arguments before handling them
        * argClarifiers() - Corresponding tags to show up in usage (i.e. `-i [count]`)
        
### Building
1. Clone repository
2. Run `mvn clean install`
3. Check `target/` for JAR

### Exceptions
Some exceptions occur when parsing parameters has issues, namely `UnhandledParameterException`.
This exception corresponds an ID that is handled by your catch block of `accept()`

| ID | Meaning                                                           |
|----|-------------------------------------------------------------------|
| 0  | Parameter requires 'x' arguments, but that many is unavailable.   |
| 1  | Parameter returned 'false' during validation in validate()        |
| 2  | Parameter is required, but not specified                          |
| 3  | The default parameter was unable to validate the input arguments. |

## Default Parameters
You can use `setDefaultParameter` to specify a "default Parameter", which handles the first set of arguments without any identifiers.
This parameter is inferred to be required, and uses the first String specified in `identifiers()` in the Usage dialog. The description is unused in this instance.

## Additional Usage Info
You can specify additional help in the `Parameter::printUsage()` dialog through specifying ``setAdditionalHelpInfo``
