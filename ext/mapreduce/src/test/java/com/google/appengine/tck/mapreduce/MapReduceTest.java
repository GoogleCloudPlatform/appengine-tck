package com.google.appengine.tck.mapreduce;

import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MapReduceTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getTckDeployment();
    }

    @Test
    public void testTODO() throws Exception {
        System.out.println("TODO -- add M/R tests!");
    }
}
