import org.junit.Test;
import static org.junit.Assert.*;


public class TestClassTest {
    @Test public void testSomeLibraryMethod() {
        TestClass classUnderTest = new TestClass();
        assertTrue("should return 'true'", classUnderTest.someMethod());
    }
}
