package com.google.appengine.tck.mapreduce.support;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * From Google AppEngine MapReduce Examples.
 *
 * @author ohler@google.com (Christian Ohler)
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CountMapper extends Mapper<Entity, String, Long> {
    private static final long serialVersionUID = 1L;

    public static String toKey(char ch) {
        return "occurrences of character " + ch + " in payload";
    }

    private void incrementCounter(String name, long delta) {
        getContext().getCounter(name).increment(delta);
    }

    private void emit(String outKey, long outValue) {
        incrementCounter(outKey, outValue);
        getContext().emit(outKey, outValue);
    }

    private void emit1(String outKey) {
        emit(outKey, 1);
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void beginShard() {
        emit1("total map shard initializations");
        emit1("total map shard initializations in shard " + getContext().getShardNumber());
    }

    @Override
    public void beginSlice() {
        emit1("total map slice initializations");
        emit1("total map slice initializations in shard " + getContext().getShardNumber());
    }

    public void map(Entity entity) {
        emit1("total entities");
        emit1("map calls in shard " + getContext().getShardNumber());
        String name = entity.getKey().getName();
        String payload = ((Text) entity.getProperty("payload")).getValue();
        emit("total entity payload size", payload.length());
        emit("total entity key size", name.length());
        for (char c = 'a'; c <= 'z'; c++) {
            emit(toKey(c), countChar(payload, c));
        }
        for (char c = '0'; c <= '9'; c++) {
            emit("occurrences of digit " + c + " in key", countChar(name, c));
        }
    }

    @Override
    public void endShard() {
        emit1("total map shard terminations");
    }

    @Override
    public void endSlice() {
        emit1("total map slice terminations");
    }

}
