#! /bin/bash

source ../env.sh
mkdir -p ./obj/clj

cp fannkuchredux.clj-12.clj ./obj/clj/fannkuchredux.clj

java -Dclojure.compile.path=./obj/clj -classpath ${CLOJURE_CLASSPATH}:./obj/clj clojure.lang.Compile fannkuchredux