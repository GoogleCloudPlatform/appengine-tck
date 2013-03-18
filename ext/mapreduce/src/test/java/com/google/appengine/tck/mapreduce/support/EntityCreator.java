package com.google.appengine.tck.mapreduce.support;

import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * From Google AppEngine MapReduce Examples.
 *
 * @author ohler@google.com (Christian Ohler)
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EntityCreator extends Mapper<Long, Void, Void> {
    private static final long serialVersionUID = 1L;

    private final String kind;
    private final List<String> payloads;
    private final Random random = new Random();

    private transient DatastoreMutationPool pool;

    public EntityCreator(String kind, List<String> payloads) {
        if (kind == null)
            throw new IllegalArgumentException("Null kind");

        this.kind = kind;
        this.payloads = payloads;
    }

    @Override
    public void beginShard() {
        pool = DatastoreMutationPool.forWorker(this);
    }

    public void map(Long index) {
        String name = String.valueOf(random.nextLong() & Long.MAX_VALUE);
        Entity e = new Entity(kind, name);
        e.setProperty("payload", new Text(payloads.get((int)(index % payloads.size()))));
        pool.put(e);
    }
}
