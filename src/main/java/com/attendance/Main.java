package com.attendance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.nio.file.Files;

/**
 * Embedded Tomcat 10 launcher for running the Attendance Management System directly.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portProp = System.getProperty("server.port");
        String portEnv = System.getenv("PORT");
        if (portProp != null && !portProp.isBlank()) {
            port = Integer.parseInt(portProp.trim());
        } else if (portEnv != null && !portEnv.isBlank()) {
            port = Integer.parseInt(portEnv.trim());
        }

        File baseDir = Files.createTempDirectory("tomcat-embedded-ams").toFile();
        baseDir.deleteOnExit();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector(); // Initialize default HTTP connector

        File webappDir = new File("src/main/webapp");
        if (!webappDir.exists()) {
            webappDir = new File("webapp");
        }
        String docBase = webappDir.getAbsolutePath();

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/attendance-management", docBase);

        File classesDir = new File("target/classes");
        if (classesDir.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    classesDir.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        tomcat.start();

        System.out.println("==================================================================");
        System.out.println(" Attendance Management System (MongoDB + Tomcat 10)");
        System.out.println(" Server successfully started!");
        System.out.println(" Access the portal at: http://localhost:" + port + "/attendance-management");
        System.out.println(" Admin Login:    admin / admin123");
        System.out.println(" Teacher Login:  teacher1 / teacher123");
        System.out.println(" Student Login:  student1 / student123");
        System.out.println("==================================================================");

        tomcat.getServer().await();
    }
}
