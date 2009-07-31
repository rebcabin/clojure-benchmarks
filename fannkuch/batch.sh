#! /bin/bash

source ../env.sh

OUTPUT_DIR=./output
mkdir $OUTPUT_DIR

BENCHMARK="fannkuch"

# No Clojure version created yet.

ALL_LANGUAGES="sbcl perl ghc java clj"
ALL_TESTS="quick long"

LANGUAGES=""
TESTS=""

while [ $# -ge 1 ]
do
    case $1 in
	sbcl|perl|ghc|java|clj) LANGUAGES="$LANGUAGES $1"
	    ;;
	quick|long) TESTS="$TESTS $1"
	    ;;
	*)
	    1>&2 echo "Unrecognized command line parameter: $1"
	    exit 1
	    ;;
    esac
    shift
done

#echo "LANGUAGES=$LANGUAGES"
#echo "TESTS=$TESTS"

if [ "x$LANGUAGES" = "x" ]
then
    LANGUAGES=${ALL_LANGUAGES}
fi

if [ "x$TESTS" = "x" ]
then
    TESTS=${ALL_TESTS}
fi

echo "LANGUAGES=$LANGUAGES"
echo "TESTS=$TESTS"

for T in $TESTS
do
    case $T in
	quick)  N=7
	    ;;
	long)   N=12
	    ;;
    esac
    for L in $LANGUAGES
    do
	case $L in
	    clj) CMD=./clj-run.sh
		;;
	    java) CMD=./java-run.sh
		( ./java-compile.sh ) >& ${OUTPUT_DIR}/java-compile-log.txt
		;;
	    sbcl) CMD=./sbcl-run.sh
		( ./sbcl-compile.sh ) >& ${OUTPUT_DIR}/sbcl-compile-log.txt
		;;
	    perl) CMD="$PERL fannkuch.perl"
		;;
	    ghc) CMD=./ghc-run.sh
		( ./ghc-compile.sh ) >& ${OUTPUT_DIR}/ghc-compile-log.txt
	esac
	
	echo
	echo "benchmark: $BENCHMARK"
	echo "language: $L"
	echo "test: $T"
	OUT=${OUTPUT_DIR}/${T}-${L}-output.txt
	CONSOLE=${OUTPUT_DIR}/${T}-${L}-console.txt
	echo "( time ${CMD} ${N} > ${OUT} ) 2>&1 | tee ${CONSOLE}"
	( time ${CMD} ${N} > ${OUT} ) 2>&1 | tee ${CONSOLE}

	$CMP ${T}-expected-output.txt ${OUT} 2>&1 | tee --append ${CONSOLE}
    done
done