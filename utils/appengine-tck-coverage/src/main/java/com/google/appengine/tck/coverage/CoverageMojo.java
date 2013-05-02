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

package com.google.appengine.tck.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @goal coverage
 * @phase process-test-classes
 * @requiresDependencyResolution
 */
public class CoverageMojo extends AbstractMojo {
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

    /**
     * Exclusion.
     *
     * @parameter
     */
    protected String exclusion;

    /**
     * Interfaces.
     *
     * @parameter
     */
    protected List<String> interfaces;

    /**
     * Coverage file.
     *
     * @parameter
     */
    protected String coverageFile = "coverage.txt";

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<URL> classPathUrls = getClassPathUrls(tests);
        ClassLoader cl = new URLClassLoader(classPathUrls.toArray(new URL[classPathUrls.size()]), getClass().getClassLoader());
        Build build = project.getBuild();
        File classesToScan = tests ? new File(build.getTestOutputDirectory()) : new File(build.getOutputDirectory());
        getLog().info("Classes to scan: " + classesToScan);
        List<String> classes = new ArrayList<String>();
        if (interfaces != null) {
            classes.addAll(interfaces);
        }
        try {
            readInterfaces(classesToScan, classes);
            MethodExclusion me = createExclusion(cl, classesToScan);
            CodeCoverage.report(cl, project.getBasedir(), classesToScan, me, classes.toArray(new String[classes.size()]));
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * Builds a classpath based on the maven project's compile classpath elements.
     *
     * @param isTest do we modify tests
     * @return The {@link ClassLoader} made up of the maven project's compile classpath elements.
     * @throws MojoExecutionException Indicates an issue processing one of the classpath elements
     */
    private List<URL> getClassPathUrls(boolean isTest) throws MojoExecutionException {
        List<URL> classPathUrls = new ArrayList<URL>();
        for (String path : projectCompileClasspathElements(isTest)) {
            try {
                getLog().debug("Adding project compile classpath element : " + path);
                classPathUrls.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Unable to build path URL [" + path + "]");
            }
        }
        return classPathUrls;
    }

    /**
     * Essentially a call to {@link MavenProject#getCompileClasspathElements} except that here we
     * cast it to the generic type and internally handle {@link org.apache.maven.artifact.DependencyResolutionRequiredException}.
     *
     * @param isTest do we modify tests
     * @return The compile classpath elements
     * @throws MojoExecutionException Indicates a {@link org.apache.maven.artifact.DependencyResolutionRequiredException} was encountered
     */
    private List<String> projectCompileClasspathElements(boolean isTest) throws MojoExecutionException {
        try {
            if (isTest) {
                return project.getTestClasspathElements();
            } else {
                return project.getCompileClasspathElements();
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Call to MavenProject#getCompileClasspathElements required dependency resolution");
        }
    }

    @SuppressWarnings("unchecked")
    private MethodExclusion createExclusion(ClassLoader cl, File root) throws Exception {
        if (exclusion != null) {
            Class<MethodExclusion> clazz = (Class<MethodExclusion>) cl.loadClass(exclusion);
            return clazz.getConstructor(File.class).newInstance(root);
        } else {
            return FileMethodExclusion.create(root);
        }
    }

    private void readInterfaces(File root, List<String> classes) throws IOException {
        String cf = System.getProperty("coverage.file", coverageFile);
        File coverage = new File(root, cf);
        if (coverage.exists()) {
            getLog().info("Reading interfaces from " + coverage);
            BufferedReader reader = new BufferedReader(new FileReader(coverage));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.startsWith("#") == false) {
                        classes.add(line);
                    }
                }
            } finally {
                reader.close();
            }
        }
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public boolean isTests() {
        return tests;
    }

    public void setTests(boolean tests) {
        this.tests = tests;
    }

    public String getExclusion() {
        return exclusion;
    }

    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getCoverageFile() {
        return coverageFile;
    }

    public void setCoverageFile(String coverageFile) {
        this.coverageFile = coverageFile;
    }
}
