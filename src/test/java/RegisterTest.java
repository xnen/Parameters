import com.github.xnen.Parameters;
import com.github.xnen.exception.ParameterException;
import com.github.xnen.impl.IHandler;
import com.github.xnen.impl.IValidator;
import com.github.xnen.param.ParamBuilder;
import com.github.xnen.param.Parameter;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;

public class RegisterTest {

    private boolean testOnePass;
    private int testTwoPass;
    private boolean testThreePass;
    private boolean testFiveFail;
    private boolean testFivePass;
    private boolean testSixPass;

    private boolean helpHandlerWorks;

    @Test
    public void testOne() {
        // Testing general use with a single identifier on a parameter.
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test")
                .description("Tests")
                .handler(args -> testOnePass = true)
                .build());

        try {
            parameters.process("--test");
            if (!testOnePass) {
                fail("Passing --test did not execute test parameter.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal use.");

        }
    }

    @Test
    public void testTwo() {
        // testOne but with two identifiers for one parameter
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests")
                .handler(args -> testTwoPass++)
                .build());

        try {
            parameters.process("--test");
            parameters.process("-t");
            if (testTwoPass != 2) {
                fail("Parameter '--test, -t' failed to execute on both valid identifiers.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal use.");
        }
    }

    @Test
    public void testThree() {
        // Test required operation

        Parameters parameters = new Parameters(args -> helpHandlerWorks = true);
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> testThreePass = true)
                .required()
                .build());

        try {
            parameters.process("-not-required-param-passed");
            fail("Required parameter was not passed, but no exception was thrown.");
        } catch (ParameterException e) {
            if (e.getId() != 1) {
                e.printStackTrace();
                fail("Wrong ParameterException thrown when expecting a 'required not met' exception.");
            }
        }

        try {
            parameters.process("--help");
            if (!helpHandlerWorks) {
                fail("Custom help handler NOT CALLED when --help is sent.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("Exception thrown when passed argument was --help.");
        }

        try {
            parameters.process("--test");
            if (!this.testThreePass) {
                fail("Required argument passed properly did not execute.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    @Test
    public void testFour() {
        // Test required operation but with an exception
        Parameters parameters = new Parameters(args -> {}); // Use special --help handler since we don't want to System.exit(0);
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> {})
                .requiredUnless("--exception")
                .build());

        parameters.register(ParamBuilder.with()
                .identifier("--exception")
                .description("Exception testing")
                .handler(args -> {})
                .build());

        try {
            parameters.process("--exception");
            parameters.process("--help");
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("Exception to required argument --test was ignored?");
        }
    }

    @Test
    public void testFive() {
        // Test case sensitivity
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> testFiveFail = true)
                .caseSensitive()
                .build());

        parameters.handleInvalidOptionsWith(args -> {
            testFivePass = true;
            System.out.println("INVALID ARG -> " + Arrays.toString(args));
        });

        try {
            parameters.process("--Test");

            if (testFiveFail) {
                fail("Case-sensitive parameter '--test' executed with an incorrect case.");
            }
            if (!testFivePass) {
                fail("Invalid options not passed to invalidOption IHandler.");
            }

            System.out.println("Processing -t in testFive...");
            parameters.process("-t");

            if (!this.testFiveFail) {
                fail("Normal usage failed to execute when -t is passed.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    @Test
    public void testSix() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> {
                    System.out.println(Arrays.toString(args));
                    if (args.length == 3) {
                        this.testSixPass = true;
                    }
                })
                .acceptsInfiniteArgs("Test!")
                .build());

        parameters.register(ParamBuilder.with()
                .identifier("--infStop", "-i")
                .description("infStop").handler(args -> {})
                .build());

        try {
            parameters.process("--test", "abc", "def", "ghi");
            if (!this.testSixPass) {
                fail("Multiple arguments failed to pass to an infinite parameter.");
            }

            this.testSixPass = false;

            parameters.process("--test", "abc", "def", "ghi", "--infStop");

            if (!this.testSixPass) {
                fail("Did not properly stop infinite accepting at a valid parameter identifier.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    private boolean testSevenPass;
    private boolean testSevenSecondPass;
    @Test
    public void testSeven() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> this.testSevenSecondPass = true)
                .build());

        parameters.setDefaultParameter(ParamBuilder.with()
                .identifier("defaultTest")
                .description("testing default")
                .handler(args -> {
                    System.out.println(Arrays.toString(args));
                    if (args.length == 1) {
                        this.testSevenPass = true;
                    }
                })
                .build());

        try {
            parameters.process("testing-default", "--test");
            if (!testSevenPass) {
                fail("Default parameter failed to accept single arg");
            }
            if (!testSevenSecondPass) {
                fail("Second parameter --test was not executed.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    private String testEightTest;
    private boolean testEightEither, testEightOr;
    @Test
    public void testEight() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> {
                    testEightTest = "A";
                    testEightEither = true;
                })
                .priority((short) 1)
                .build());
        parameters.register(ParamBuilder.with()
                .identifier("--higher", "-h")
                .description("Tests").handler(args -> {
                    testEightTest = "B";
                    testEightOr = true;
                })
                .priority((short) 2)
                .build());

        try {
            parameters.process("--test", "--higher");
            if (!testEightTest.equals("A")) {
                fail("--higher should have processed first, as it has higher priority. But alas, " + testEightTest + " was the result.");
            }
            if (!testEightEither || !testEightOr) {
                fail("One of the parameters failed to execute.");
                System.out.println(testEightEither + ", " + testEightOr);
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    private boolean testNinePass;

    @Test
    public void testNine() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> {
                    if (args[0].equals("onearg")) {
                        testNinePass = true;
                    }
                })
                .acceptArg("accept-arg")
                .build());

        try {
            parameters.process("--test", "onearg");
            if (!testNinePass) {
                fail("Failed to pass 'onearg' to --test, when it should be accepting one argument.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }

        try {
            parameters.process("--test");
            fail("--test was allowed to work without any arguments, when one is expected.");
        } catch (ParameterException e) {
            if (e.getId() != 0) {
                e.printStackTrace();
                fail("Unexpected ParameterException when expecting param count exception.");
            }
        }
    }

    private boolean testTenFail;
    @Test
    public void testTen() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> testTenFail = true)
                .caseSensitive()
                .build());

        try {
            parameters.process();
            if (testTenFail) {
                fail("--test parameter executed when not called.");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    @Test
    public void testEleven() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> testFiveFail = true)
                        .validator(args -> args[0].equals("TESTING"))
                        .acceptArg("testArg")
                .build());

        try {
            parameters.process("--test", "TESTING");
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }

        try {
            parameters.process("--test", "BADPARAM");
            fail("Improper validation of BADPARAM.");
        } catch (ParameterException e) {
            if (e.getId() != 2) {
                e.printStackTrace();
                fail("ParameterException thrown in normal usage.");
            }
        }
    }

    private boolean testTwelvePass;
    private boolean testTwelveFail;

    @Test
    public void testTwelve() {
        Parameters parameters = new Parameters("testJar.jar", "Testing");
        parameters.register(ParamBuilder.with()
                .identifier("--test", "-t")
                .description("Tests").handler(args -> testFiveFail = true)
                .build());

        parameters.handleInvalidOptionsWith(args -> {
            System.out.println("INVALID ->" + Arrays.toString(args));
            if (args[0].equals("unhandled")) {
                testTwelvePass = true;
            }
        });

        parameters.setDefaultParameter(ParamBuilder.with().identifier("default").description("default").handler(args -> {
            if (args.length != 1 || !args[0].equals("help")) {
                System.out.println(Arrays.toString(args));
                testTwelveFail = true;
            }
        }).build());

        try {
            parameters.process("help", "--test");
            if (testTwelveFail) {
                fail("Default parameter did not receive 'help' as its argument.");
            }
            parameters.process("help", "--test", "unhandled");
            if (testTwelveFail) {
                fail("Default parameter accepted the wrong argument.");
            }
            if (!testTwelvePass) {
                fail("'unhandled' not passed to unhandled IHandler. ");
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    private boolean helpCalled;
    private boolean handledFile;
    private boolean settingsCleared;

    private int numUnhandled;
    private String[] unhandledThirteen;

    @Test
    public void testThirteen() {
        Parameters parameters = new Parameters(args -> RegisterTest.this.helpCalled = true);

        parameters.register(
                ParamBuilder.with().identifier("--clear-settings").description("Clear all settings and restore defaults")
                        .handler(args -> RegisterTest.this.settingsCleared = true)
                        .priority((short) 10)
                        .build()
        );

        parameters.setDefaultParameter(ParamBuilder.with().identifier("*.*").description("A File")
                .handler(strings -> {
                    RegisterTest.this.handledFile = true;
                    System.out.println(Arrays.toString(strings));
                })
                .build());

        IHandler unhandled = args -> {
            System.out.println(Arrays.toString(args));
            numUnhandled = args.length;
            unhandledThirteen = args;
        };

        parameters.handleInvalidOptionsWith(unhandled);

        try {
            parameters.accept("--help");
            if (!this.helpCalled) {
                fail("--help failed to call when default parameter is present.");
            }
            if (this.handledFile) {
                fail("Something went horribly wrong.");
            }
            if (numUnhandled > 0) {
                fail("Unexpected unhandled args");
            }

            helpCalled = false;

            parameters.accept("file.txt", "--clear-settings");
            if (!this.handledFile) {
                fail("file.txt failed to accept into param handler");
            }
            if (!this.settingsCleared) {
                fail("Failed to clear settings");
            }
            if (numUnhandled > 0) {
                fail("Unexpected unhandled args");
            }

            handledFile = false;
            settingsCleared = false;

            parameters.accept("file1.txt", "-invalid-option", "--clear-settings");
            if (this.handledFile) {
                fail("File handled when invalid option exists.");
            }
            if (settingsCleared) {
                fail("Settings cleared when invalid option exists.");
            }

            if (numUnhandled < 1) {
                fail("Expected unhandled arg, didn't get any.");
            }
            if (numUnhandled > 1) {
                fail("More unhandled args than expected.");
            }
            if (!unhandledThirteen[0].equals("-invalid-option")) {
                fail("Passed unhandled arg is not what I expected.");
            }

            numUnhandled = 0;

            // Default arg isnt required
            parameters.accept("--clear-settings");

            if (!settingsCleared) {
                fail("Settings should have cleared, but hadn't.");
            }

        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }
    }

    private String[] unhandledFourteen;

    private String helpOrFile;

    @Test
    public void testFourteen() {
        this.helpCalled = false;
        this.handledFile = false;
        this.settingsCleared = false;
        this.numUnhandled = 0;

        Parameters parameters = new Parameters(args -> {
            this.helpOrFile = "help";
            RegisterTest.this.helpCalled = true; });

        parameters.register(
                ParamBuilder.with().identifier("--clear-settings").description("Clear all settings and restore defaults")
                        .handler(args -> RegisterTest.this.settingsCleared = true)
                        .priority((short) 10)
                        .build()
        );

        parameters.setDefaultParameter(ParamBuilder.with().identifier("*.*").description("A File")
                .handler(strings -> {
                    RegisterTest.this.handledFile = true;
                    this.helpOrFile = "file";
                    System.out.println(Arrays.toString(strings));
                })
                .required().build());

        IHandler unhandled = args -> {
            System.out.println(Arrays.toString(args));
            numUnhandled = args.length;
            unhandledFourteen = args;
        };

        parameters.handleInvalidOptionsWith(unhandled);

        try {
            parameters.process("--help");
            if (!this.helpCalled) {
                fail("--help failed to be called.");
            }

            this.helpCalled = false;
            this.handledFile = false;

            parameters.process("test.txt", "--help");
            if (this.helpCalled) {
                if (this.handledFile) {
                    if (this.helpOrFile.equals("help")) {
                        fail("Default param handled before --help, or not called at all?");
                    }
                }
            } else {
                fail("--help failed to be called with default param satisfied.");
            }

            this.helpCalled = false;
        } catch (ParameterException e) {
            e.printStackTrace();
            fail("ParameterException thrown in normal usage.");
        }

        if (numUnhandled > 0) {
            fail("Unhandled higher than expected before unhandled test.");
        }

        try {
            parameters.process();
            fail("Required default arg is not satisfied, but allowed to continue anyway.");

            parameters.process("test1.txt", "test2.txt");
            if (numUnhandled < 1) {
                fail("Expected unhandled arg, but got none.");
            }
            if (numUnhandled > 1) {
                fail("Expected only one unhandled arg, but got one.");
            }

            if (!unhandledFourteen[0].equals("test2.txt")) {
                fail("Unexpected unhandled arg. Should be 'test2.txt', but got " + unhandledFourteen[0]);
            }

        } catch (ParameterException e) {
            // ignored
        }
    }
}
