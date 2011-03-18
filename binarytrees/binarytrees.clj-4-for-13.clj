;; The Computer Language Benchmarks Game
;; http://shootout.alioth.debian.org/
;
;; Adapted from the Java -server version
;
;; contributed by Marko Kocic
;; modified by Kenneth Jonsson, restructured to allow usage of 'pmap'
;; modified by Andy Fingerhut to use faster primitive math ops, and
;; deftype instead of defrecord for smaller tree nodes.
;; https://github.com/jafingerhut/clojure-benchmarks/blob/master/binarytrees/binarytrees.clj-4.clj modified by RH for 1.3

(ns binarytrees
  (:gen-class))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(definterface ITreeNode
  (^long item [])
  (left [])
  (right []))

;; These TreeNode's take up noticeably less memory than a similar one
;; implemented using defrecord.

(deftype TreeNode [left right ^long item]
  ITreeNode
  (item [this] item)
  (left [this] left)
  (right [this] right))

(defn bottom-up-tree [^long item ^long depth]
  (if (zero? depth)
    (TreeNode. nil nil item)
    (TreeNode.
     (bottom-up-tree (dec (* 2 item))
                     (dec depth))
     (bottom-up-tree (* 2 item)
                     (dec depth))
     item)))

(defn item-check ^long [^TreeNode node]
  (if (nil? (.left node))
    (.item node)
    (+ (+ (.item node)
          (item-check (.left node)))
       (- (item-check (.right node))))))

(defn iterate-trees [^long mx ^long mn ^long d]
  (let [iterations (bit-shift-left 1 (- (+ mx mn) d))]
    (format "%d\t trees of depth %d\t check: %d" (* 2 iterations) d
            (reduce + (map (fn [i]
                             (+ (item-check (bottom-up-tree i d))
                                (item-check (bottom-up-tree (- i) d))))
                           (range 1 (inc iterations)))))))

(def min-depth 4)

(defn main [max-depth]
  (let [stretch-depth (inc max-depth)]
    (let [tree (bottom-up-tree 0 stretch-depth)
          check (item-check tree)]
      (println (format "stretch tree of depth %d\t check: %d" stretch-depth check)))
    (let [long-lived-tree (bottom-up-tree 0 max-depth) ]
      (doseq [trees-nfo (map (fn [d]
                                (iterate-trees max-depth min-depth d))
(range min-depth stretch-depth 2)) ]
        (println trees-nfo))
      (println (format "long lived tree of depth %d\t check: %d" max-depth (item-check long-lived-tree))))))

(defn -main [& args]
  (let [n (if (first args) (Integer/parseInt (first args)) 0)
        max-depth (if (> (+ min-depth 2) n) (+ min-depth 2) n)]
    (main max-depth)
    (shutdown-agents)))

(comment
;;sample usage
(in-ns 'binarytrees)
(time (main 20))
)
