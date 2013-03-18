package com.google.appengine.tck.mapreduce.support;

import com.google.appengine.tools.mapreduce.KeyValue;
import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.ReducerInput;

/**
 * From Google AppEngine MapReduce Examples.
 *
 * @author ohler@google.com (Christian Ohler)
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CountReducer extends Reducer<String, Long, KeyValue<String, Long>> {
    private static final long serialVersionUID = 1L;

    private void emit(String key, long outValue) {
        //log.info("emit(" + outValue + ")");
        getContext().emit(KeyValue.of(key, outValue));
    }

    @Override
    public void reduce(String key, ReducerInput<Long> values) {
        long total = 0;
        while (values.hasNext()) {
            long value = values.next();
            total += value;
        }
        emit(key, total);
    }
}
