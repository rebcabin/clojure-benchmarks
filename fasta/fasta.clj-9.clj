;;   The Computer Language Benchmarks Game
;;   http://shootout.alioth.debian.org/

;; contributed by Bill James
;; speed improvements by Andy Fingerhut

;; WARNING: This program likely violates the intent of the fasta
;; problem, which is that the random number generator should be called
;; once for each random DNA sequence character output in the two
;; random sequences.  This technique should not be emulated for
;; programs submitted to the web site.

(ns fasta
  (:gen-class))

(set! *warn-on-reflection* true)


(def *width* 60)
(def *lookup-size* 222000)


(def *alu* (str "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
                "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
                "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
                "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
                "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
                "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
                "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA"))

(def *codes* "acgtBDHKMNRSVWY")

(def *iub* [0.27 0.12 0.12 0.27 0.02 0.02 0.02 0.02
           0.02 0.02 0.02 0.02 0.02 0.02 0.02])

(def *homosapiens* [0.3029549426680 0.1979883004921
                   0.1975473066391 0.3015094502008])


(defn find-index [f coll]
  (loop [i (int 0)
         s (seq coll)]
    (if (f (first s))
      i
      (recur (unchecked-inc i) (rest s)))))


(def random-seed (int-array [42]))
(let [IM (int 139968)
      IA (int 3877)
      IC (int 29573)
      scale (double (/ *lookup-size* IM))]
  (defn gen-random-fast []
    (let [new-seed (unchecked-remainder
                    (unchecked-add (unchecked-multiply
                                    (aget (ints random-seed) 0) IA) IC) IM)]
      (aset (ints random-seed) 0 new-seed)
      (int (* new-seed scale)))))


;; Takes a vector of probabilities.
(defn make-cumulative [v]
  (vec (map #(reduce + (subvec v 0 %)) (range 1 (inc (count v))))))


;; Takes a vector of cumulative probabilities.
(defn make-lookup-table [v]
  (let [sz (int *lookup-size*)
        lookup-scale (- sz 0.0001)
        ^ints a (int-array sz)]
    (dotimes [i sz]
      (aset a i (int (find-index #(<= (/ i lookup-scale) %) v))))
    a))


(defn cycle-bytes [source source-size n
                   ^java.io.BufferedOutputStream ostream]
  (let [source-size (int source-size)
        width (int *width*)
        width+1 (int (inc width))
        buffer-size (int (* width+1 4096))
        buffer (byte-array buffer-size (byte 10))]
    (loop [i (int 0)
           j (int 0)
           n (int n)]
      (System/arraycopy source i buffer j width)
      (if (> n width)
        (recur (int (unchecked-remainder
                     (unchecked-add i width) source-size))
               (int (let [j (unchecked-add j width+1)]
                      (if (== j buffer-size)
                        (do (.write ostream buffer) (int 0))
                        j)))
               (unchecked-subtract n width))
        (do
          (aset buffer (+ j n) (byte 10))
          (.write ostream buffer (int 0) (+ j n 1)))))))


(defn fasta-repeat [n ^java.io.BufferedOutputStream ostream]
  (let [source (.getBytes (str *alu* *alu*))]
    (cycle-bytes source (count *alu*) n ostream)))


(defn fasta-random [probs n ^java.io.BufferedOutputStream ostream]
  (let [codes (.getBytes (str *codes*))
        lookup-table (ints (make-lookup-table
                            (make-cumulative probs)))
        width (int *width*)
        buffer (byte-array 222000)
        seeds  (int-array  222000)
        first-seed (aget (ints random-seed) 0)]
    (loop [i (int 0)]
      (aset seeds i (aget (ints random-seed) 0))
      (aset buffer i
            (aget codes
                  (aget lookup-table
                        (gen-random-fast))))
      (if (== (aget (ints random-seed) 0) first-seed)
        (do
          (System/arraycopy  buffer 0  buffer (inc i)  *width*)
          (cycle-bytes buffer (inc i) n ostream)
          (aset (ints random-seed) 0 (aget seeds (mod n (inc i)))))
        (recur (unchecked-inc i))))))


(defn write-line [s ^java.io.BufferedOutputStream stream]
  (.write stream (.getBytes (str s "\n"))))


(defn -main [& args]
  (let [n (Integer/parseInt (nth args 0))
        ostream (java.io.BufferedOutputStream. System/out)
        start-time (System/currentTimeMillis)]
    (write-line ">ONE Homo sapiens alu" ostream)
    (fasta-repeat (* n 2) ostream)
    (write-line ">TWO IUB ambiguity codes" ostream)
    (fasta-random *iub* (* n 3) ostream)
    (write-line ">THREE Homo sapiens frequency" ostream)
    (fasta-random *homosapiens* (* n 5) ostream)
    (.flush ostream)))
