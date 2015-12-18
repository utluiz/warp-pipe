import org.junit.Test;


public class LauncherTest {
    @Test
    public void testSomeLibraryMethod() {
        TomcatLauncher classUnderTest = new TomcatLauncher();
        try {
            classUnderTest.launchTomcat(8080);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //assertTrue("should return 'true'", classUnderTest.someMethod());
    }
}
