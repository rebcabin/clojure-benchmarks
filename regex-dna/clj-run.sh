#! /bin/bash

# Read input from stdin
# Write output to stdout

source ../env.sh

JVM_MEM_OPTS="-client -Xmx1024m"

#JMX_MONITORING=-Dcom.sun.management.jmxremote

CLJ_PROG=regexdna.clj-1.clj

$JAVA $JVM_MEM_OPTS $JMX_MONITORING ${JAVA_PROFILING} -cp ${CLOJURE_CLASSPATH} clojure.main $CLJ_PROG "$@"
