package com.google.appengine.tck.base;

import java.util.Properties;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestContext implements Cloneable {
    private String archiveName;

    private String webXmlContent = "<web/>";
    private String webXmlFile;

    private String appEngineWebXmlFile;

    private ContextRoot contextRoot;

    private String compatibilityProperties;
    private Properties properties = new Properties();

    private boolean callbacks;

    public TestContext() {
    }

    public TestContext(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public TestContext setArchiveName(String archiveName) {
        this.archiveName = archiveName;
        return this;
    }

    public String getWebXmlContent() {
        return webXmlContent;
    }

    public TestContext setWebXmlContent(String webXmlContent) {
        this.webXmlContent = webXmlContent;
        return this;
    }

    public String getWebXmlFile() {
        return webXmlFile;
    }

    public TestContext setWebXmlFile(String webXmlFile) {
        this.webXmlFile = webXmlFile;
        return this;
    }

    public String getAppEngineWebXmlFile() {
        return appEngineWebXmlFile;
    }

    public TestContext setAppEngineWebXmlFile(String appEngineWebXmlFile) {
        this.appEngineWebXmlFile = appEngineWebXmlFile;
        return this;
    }

    public ContextRoot getContextRoot() {
        return contextRoot;
    }

    public TestContext setContextRoot(ContextRoot contextRoot) {
        this.contextRoot = contextRoot;
        return this;
    }

    public String getCompatibilityProperties() {
        return compatibilityProperties;
    }

    public TestContext setCompatibilityProperties(String compatibilityProperties) {
        this.compatibilityProperties = compatibilityProperties;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean hasCallbacks() {
        return callbacks;
    }

    public TestContext setCallbacks(boolean callbacks) {
        this.callbacks = callbacks;
        return this;
    }
}
