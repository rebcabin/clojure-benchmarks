#! /bin/bash

for j in binarytrees fannkuch fannkuchredux fasta knucleotide mandelbrot nbody regexdna revcomp revlines spectralnorm
do
    for CLJ_VERSION in clj-1.2 clj-1.3-alpha1 clj-1.3-alpha3 clj-1.3-alpha4
    do
        cd $j
        \rm -fr obj/${CLJ_VERSION}
        ./clj-compile.sh ${CLJ_VERSION}
        cd ..
    done
done
