/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.jsp;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.util.DefaultClassIntrospector;
import io.undertow.testutils.DefaultServer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

/**
 * This plugin compiles .jsp files.
 *
 * It uses UnderTow's DefaultServer JUnit runner,
 * and own Mojo class as a test class, to execute compile method.
 * It then only touches .jsp pages with ?jsp_precompile,
 * hence not executing the .jsp page, but only pre-compiling it.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @goal jsp
 * @phase test-compile
 * @requiresDependencyResolution
 */
public class JspMojo extends AbstractMojo {
    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * Tests.
     *
     * @parameter
     */
    protected boolean tests = true;

    private static ThreadLocal<JspMojo> TL = new ThreadLocal<>();

    protected File getJspLocation() {
        Build build = project.getBuild();

        final List<Resource> jsps = (tests ? build.getTestResources() : build.getResources());
        if (jsps == null || jsps.isEmpty()) {
            throw new IllegalStateException(String.format("No resources defined, build: %s", build));
        }

        return new File(jsps.get(0).getDirectory());
    }

    protected File getTempDir() {
        Build build = project.getBuild();
        return (new File(tests ? build.getTestOutputDirectory() : build.getOutputDirectory()));
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        final RunNotifier notifier = new RunNotifier();
        try {
            DefaultServer server = new DefaultServer(JspMojo.class);
            TL.set(this);
            try {
                server.run(notifier);
            } finally {
                TL.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Cannot compile JSP files.", e);
        } finally {
            notifier.fireTestRunFinished(new Result());
        }
    }

    @Test
    public void compile() throws Exception {
        final PathHandler servletPath = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();

        JspMojo mojo = TL.get();

        final File root = mojo.getJspLocation();

        getLog().info(String.format("JSP location: %s", root));

        final FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jsp");
            }
        };

        ServletInfo servlet = JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp");
        servlet.addInitParam("mappedfile", Boolean.TRUE.toString());

        DeploymentInfo builder = new DeploymentInfo()
            .setClassLoader(JspMojo.class.getClassLoader())
            .setContextPath("/tck")
            .setClassIntrospecter(DefaultClassIntrospector.INSTANCE)
            .setDeploymentName("tck.war")
            .setResourceManager(new FileResourceManager(root, Integer.MAX_VALUE))
            .setTempDir(mojo.getTempDir())
            .setServletStackTraces(ServletStackTraces.NONE)
            .addServlet(servlet);
        JspServletBuilder.setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), new HashMap<String, TagLibraryInfo>(), new HackInstanceManager());

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        servletPath.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(servletPath);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (File jsp : root.listFiles(filter)) {
                touchJsp(client, jsp.getName());
            }
        }
    }

    protected void touchJsp(HttpClient client, String name) {
        getLog().info(String.format("Touching %s file.", name));
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/tck/" + name + "?jsp_precompile");
            client.execute(get); // just touch, so it compiles .jsp, ignore any error
        } catch (Throwable ignored) {
        }
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setTests(boolean tests) {
        this.tests = tests;
    }
}
