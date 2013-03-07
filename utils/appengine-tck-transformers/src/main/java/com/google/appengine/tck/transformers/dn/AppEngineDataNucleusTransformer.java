package com.google.appengine.tck.transformers.dn;

import com.google.appengine.tck.transformers.ArquillianJUnitTransformer;
import com.google.appengine.tck.transformers.MatchingClassFileTransformer;
import javassist.CtClass;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AppEngineDataNucleusTransformer extends ArquillianJUnitTransformer implements MatchingClassFileTransformer {
    public boolean match(String className) {
        return className.endsWith("Test");
    }

    protected String getDeploymentMethodBody(CtClass clazz) throws Exception {
        return "{return com.google.appengine.tck.transformers.dn.AppEngineDataNucleusTransformer.buildArchive(\"" + clazz.getName() + "\");}";
    }

    public static WebArchive buildArchive(String clazz) {
        WebArchive war = createWar();
        addClasses(war, clazz);

        war.addPackage("com.google.appengine.datanucleus");

        war.addClass("com.google.appengine.datanucleus.jpa.JPATestCase$EntityManagerFactoryName");
        war.addClass("com.google.appengine.datanucleus.jdo.JDOTestCase$PersistenceManagerFactoryName");

        war.addPackage("com.google.appengine.datanucleus.query");

        war.addPackage("com.google.appengine.datanucleus.test.jdo");
        war.addPackage("com.google.appengine.datanucleus.test.jpa");

        war.setWebXML(new org.jboss.shrinkwrap.api.asset.StringAsset("<web/>"));
        war.addAsWebInfResource("appengine-web.xml");
        war.addAsWebInfResource("META-INF/persistence.xml", "classes/META-INF/persistence.xml");
        war.addAsWebInfResource("META-INF/jdoconfig.xml", "classes/META-INF/jdoconfig.xml");
        war.addAsResource(new StringAsset("ignore.logging=true\n"), "capedwarf-compatibility.properties");

        final PomEquippedResolveStage resolver = getResolver("pom.xml");
        // GAE DN libs
        war.addAsLibraries(resolve(resolver, "com.google.appengine.orm:datanucleus-appengine"));
        war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-api-1.0-sdk"));
        war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-testing"));
        war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-api-stubs"));
        war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-core"));
        war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-api-jdo"));
        war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-api-jpa"));
        war.addAsLibraries(resolve(resolver, "javax.jdo:jdo-api"));
        war.addAsLibraries(resolve(resolver, "org.apache.geronimo.specs:geronimo-jpa_2.0_spec"));
        war.addAsLibraries(resolve(resolver, "org.easymock:easymock"));
        war.addAsLibraries(resolve(resolver, "org.easymock:easymockclassextension"));
        // TCK Internals
        war.addAsLibraries(resolve(resolver, "com.google.appengine.tck:appengine-tck-transformers")); // cleanup dep
        war.addAsLibraries(resolve(resolver, "com.google.appengine.tck:appengine-tck-base")); // lifecycle dep

        return war;
    }

    private static void addClasses(WebArchive war, String clazz) {
        try {
            ClassLoader cl = AppEngineDataNucleusTransformer.class.getClassLoader();
            Class<?> current = cl.loadClass(clazz);
            addClasses(war, current);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClasses(WebArchive war, Class<?> current) {
        while (current != null && current != Object.class && "junit.framework.TestCase".equals(current.getName()) == false) {
            war.addClass(current);
            current = current.getSuperclass();
        }
    }

    @Override
    protected String tearDownSrc() {
        return "com.google.appengine.tck.transformers.TestUtils.clean();" + super.tearDownSrc();
    }
}
