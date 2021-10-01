# Parameters
Basic parameter handling library for Java console applications

**Severe WIP**, bugs are guaranteed, features are likely to completely not work.
Not much tested yet, including building.

### Getting Started:

1. **Create a Parameters object.**
 
    If you would like to use the default help dialog, you can simply use `new Parameters(JAR_NAME, JAR_DESCRIPTION)`.

    Otherwise, create your own by specifying `new Parameters(IHandler)`. Any code written in `handle(String... args)` will be executed when the user writes either `--help` or `-?`.


2. **Register your Parameters** using `Parameters::register()`. You can easily create Parameters using the ParamBuilder: `ParamBuilder.with()...`

 The ParamBuilder gives a few options to use for your Parameter, namely
 - 'caseSensitive()' (Case sensitive?)
 - 'required()' (Required?)
 - 'requiredUnless()' (Required, with cases where it's not?)
 - 'priority()' (How early should it be handled, regardless of order?)
 - 'validator()' (Are the arguments passed to this parameter valid?)
 - 'acceptArg()' (Should the parameter accept any additional arguments?)
 - 'acceptInfiniteArgs()' (Should the parameter accept all arguments up to an identifier?)

The `validator()` is a pre-check of args that are passed to the Parameter. You may return FALSE to stop the parameters from executing/handling if an argument doesn't look right.

`acceptArg()` and `acceptInfiniteArgs()` allow you to specify that this Parameter requires an argument. The string associated with each shows up in a usage dialog for users to know what to input.

`requiredUnless()` states that the Parameter is required, unless the specified identifier is present. ALL parameters are not required if `--help` or `-?` is present (and won't be run).

3. **You can also register a 'default parameter'** using `setDefaultParameter()`. This will accept the first 'x' numbers of invalid arguments, depending on how many arguments are specified.

   *Please Note* that default parameters always accept one arg (itself), and that arg is **always required**!

4. **You can specify an IHandler to handle all invalid arguments** not accepted by the default parameter (if there is any) with `Parameters::handleInvalidOptionsWith()`

5. Finally, **pass your `public static void main(String[] args)` args to `process()`**

#### Example:

```
    parameters.register(
               ParamBuilder.with()
              .identifier("--test", "-t")
              .description("This is an example!")
              .handler(args -> {
                 System.out.println("Hello world!");
              }).build());
```

### Building
1. Clone repository
2. Run `mvn clean install`
3. Check `target/` for JAR

### Exceptions
Some exceptions occur when parsing parameters has issues, namely `ParameterException`.
This exception corresponds an ID that is handled by your catch block of `process()`

| ID | Meaning                                                           |
|----|-------------------------------------------------------------------|
| 0  | Parameter consumes more arguments than are available.             |
| 1  | Parameter is required, but not present/specified.                 |
| 2  | Parameter returned FALSE during validation.                       |