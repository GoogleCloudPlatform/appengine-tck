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

package com.google.appengine.tck.memcache.support;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * Contains fields of various types for testing.
 */
public class ComboType implements Serializable {
    // for Serializable
    public ComboType() {
    }

    public ComboType(int intField, long longField, String stringField, Date dateField) {
        this.intField = intField;
        this.longField = longField;
        this.stringField = stringField;
        this.dateField = dateField;
        this.mixedField = new ArrayList<Object>();
        mixedField.add(intField);
        mixedField.add(longField);
        mixedField.add(stringField);
        mixedField.add(dateField);
    }

    private String stringField;

    private int intField = 0;

    private long longField = 0L;

    private Date dateField;

    private Collection<Object> mixedField; // for testing value of mixed types.

    public final Collection<Object> getMixedField() {
        return mixedField;
    }

    public final void setMixedField(Collection<Object> mixedField) {
        this.mixedField = mixedField;
    }

    public final String getStringField() {
        return stringField;
    }

    public final void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public final int getIntField() {
        return intField;
    }

    public final void setIntField(int intField) {
        this.intField = intField;
    }

    public final long getLongField() {
        return longField;
    }

    public final void setLongField(long longField) {
        this.longField = longField;
    }

    public final Date getDateField() {
        return dateField;
    }

    public final void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    // fields that are included for comparison
    private static final String[] FIELDS = {"intField", "longField", "stringField", "dateField", "mixedField"};

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ComboType)) {
            return false;
        }

        for (String name : FIELDS) {
            try {
                Field field = ComboType.class.getDeclaredField(name);
                if (!isEqual(field.get(o), field.get(this))) {
                    return false;
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    // a generic helper method
    private static boolean isEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (String name : FIELDS) {
            try {
                Field field = ComboType.class.getDeclaredField(name);
                Object obj = field.get(this);
                if (obj != null) {
                    h += obj.hashCode();
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ComboType[");
        for (String name : FIELDS) {
            try {
                Field field = ComboType.class.getDeclaredField(name);
                sb.append(name);
                sb.append("=");
                sb.append(field.get(this));
                sb.append(",");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }
}
