Byteman usage in container -- add this to your JAVA_OPTS:

JAVA_OPTS="$JAVA_OPTS -Xbootclasspath/p:${BYTEMAN_HOME}/lib/byteman-submit.jar -javaagent:${BYTEMAN_HOME}/lib/byteman.jar=port:9091"

