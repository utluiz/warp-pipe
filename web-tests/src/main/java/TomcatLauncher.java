import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import javax.servlet.ServletException;
import java.io.File;
import java.nio.file.Paths;

public class TomcatLauncher {

    public void launchTomcat(int port) throws ServletException, LifecycleException {
        String webappDirLocation = Paths.get("src/main/webapp/").toAbsolutePath().toString();
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", webappDirLocation);
        System.out.println("configuring app with basedir: " + webappDirLocation);

        // Declare an alternative location for your "WEB-INF/classes" dir
        // Servlet 3.0 annotation will work
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();
    }

}
