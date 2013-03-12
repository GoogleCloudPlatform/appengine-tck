package com.google.appengine.tck.coverage;

import javassist.bytecode.ConstPool;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BytecodeUtils {
    static String getClassName(ConstPool pool, int i) {
        return getRef(pool, i).getClassName(pool, i);
    }

    static String getName(ConstPool pool, int i) {
        return getRef(pool, i).getName(pool, i);
    }

    static String getDesc(ConstPool pool, int i) {
        return getRef(pool, i).getDesc(pool, i);
    }

    static Ref getRef(ConstPool pool, int i) {
        int tag = pool.getTag(i);
        switch (tag) {
            case ConstPool.CONST_Methodref:
                return RefType.METHOD;
            case ConstPool.CONST_InterfaceMethodref:
                return RefType.INTERFACE_METHOD;
            default:
                return RefType.NOOP;
        }
    }

    static interface Ref {
        int getTag();
        String getClassName(ConstPool pool, int i);
        String getName(ConstPool pool, int i);
        String getDesc(ConstPool pool, int i);
    }

    private static enum RefType implements Ref {
        NOOP {
            public int getTag() {
                return -1;
            }

            public String getClassName(ConstPool pool, int i) {
                return null;
            }

            public String getName(ConstPool pool, int i) {
                return null;
            }

            public String getDesc(ConstPool pool, int i) {
                return null;
            }
        },
        METHOD {
            public int getTag() {
                return ConstPool.CONST_Methodref;
            }

            public String getClassName(ConstPool pool, int i) {
                return pool.getMethodrefClassName(i);
            }

            public String getName(ConstPool pool, int i) {
                return pool.getMethodrefName(i);
            }

            public String getDesc(ConstPool pool, int i) {
                return pool.getMethodrefType(i);
            }
        },
        INTERFACE_METHOD {
            public int getTag() {
                return ConstPool.CONST_InterfaceMethodref;
            }

            public String getClassName(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefClassName(i);
            }

            public String getName(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefName(i);
            }

            public String getDesc(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefType(i);
            }
        }
    }
}
