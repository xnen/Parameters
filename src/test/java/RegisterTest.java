import com.github.xnen.Parameters;
import com.github.xnen.exception.UnhandledParameterException;
import com.github.xnen.param.IParameter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class RegisterTest {
    private boolean handled;
    private int multiHandleCount = 0;

    private String both = "";

    private List<String> unrecognized;

    @Test
    public void runTest() {
        Parameters parameters = new Parameters("Test.jar", "Tests things", params -> unrecognized = params);

        parameters.register(new IParameter() {
            public void handle(String[] args) {
                RegisterTest.this.handled = true;
                RegisterTest.this.both = "After";
            }
            public String[] identifiers() {
                return new String[] {
                        "--test"
                };
            }
            public String description() {
                return "A Test";
            }
            public boolean required() {
                return true;
            }
        });

        // Test single handling
        try {
            parameters.accept(new String[] { "--test" });
        } catch (UnhandledParameterException e) {
            fail("Normal usage of one required parameter resulted in UnhandledParameterException");
            e.printStackTrace();
        }

        parameters.register(new IParameter() {
            @Override
            public void handle(String[] args) {
                RegisterTest.this.multiHandleCount++;
                RegisterTest.this.both = "Before";
            }

            @Override
            public String[] identifiers() {
                return new String[] {
                        "-m",
                        "--multiple-iDentifiers"
                };
            }

            @Override
            public String description() {
                return "Multiple identifiers";
            }

            @Override
            public boolean caseSensitive() {
                return true;
            }

            @Override
            public boolean required() {
                return true;
            }

            @Override
            public short priority() {
                return 1;
            }
        });


        // Was it added properly?
        assertEquals(2, parameters.getRegisteredParamCount() - 1);

        // Test required argument
        try {
            parameters.accept(new String[] { "--notRequired " });
            fail("Required argument was allowed to not exist.");
        } catch (UnhandledParameterException e) {
            // ignored
        }

        // Test case-sensitive argument
        try {
            int test = this.multiHandleCount;
            parameters.accept(new String[] { "--multiple-identifiers", "--test" });
            if (this.multiHandleCount > test) {
                fail("Case-sensitive argument was handled when case was incorrect.");
            }
        } catch (UnhandledParameterException e) {
            fail("Normal usage resulted in UnhandledParameterException");
            e.printStackTrace();
        }

        // Test multiple identifiers
        try {
            parameters.accept(new String[] { "--multiple-iDentifiers", "--test" });
            parameters.accept(new String[] { "-m", "--test" });

            assertEquals(2, this.multiHandleCount);
        } catch (UnhandledParameterException e) {
            fail("Normal usage resulted in UnhandledParameterException");
            e.printStackTrace();
        }

        // Test priority
        try {
            this.handled = false;
            this.multiHandleCount = 0;
            parameters.accept(new String[] { "--test", "-m" });
            assertEquals("After", this.both);
            assertTrue(this.handled);
            assertEquals(1, this.multiHandleCount);
            // Since '-m' has a higher priority than '--test', '--test' should handle AFTER -m.
            // Meaning, "After" is written to this.both AFTER "Before" is written.

        } catch (UnhandledParameterException e) {
            fail("Normal usage resulted in UnhandledParameterException");
            e.printStackTrace();
        }

        this.unrecognized.clear();

        // Test unrecognized behavior
        try {
            parameters.accept(new String[] { "--test", "-m"});

            if (this.unrecognized != null && this.unrecognized.size() > 0) {
                System.out.println("== unrecognized == ");
                for (String s : this.unrecognized) {
                    System.out.println(s);
                }
                System.out.println(" === ");
                fail("Unrecognized options exist but are unexpected.");
            }

            parameters.accept(new String[] { "--zerg", "--test", "-m"});

            if (this.unrecognized != null && this.unrecognized.size() > 1) {
                fail("Expected 1 unrecognized parameter, but got " + this.unrecognized.size() + ".");
            } else if (this.unrecognized == null) {
                fail("Unrecognized option recognized?");
            } else if (!unrecognized.get(0).equals("--zerg")) {
                fail("Unrecognized option was not expected. (" + unrecognized.get(0) + ")");
            }

        } catch (UnhandledParameterException e) {
            fail("Normal usage resulted in UnhandledParameterException");
            e.printStackTrace();
        }

    }
}
