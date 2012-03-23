(ns ClusterAnalysis
  (:use MahoutTest)
  (:use (incanter core stats datasets charts)))

(def annotated-players (annotate-clusters (Mongo2CSV/get-all-data)))

(def cluster-keys (get-cluster-keys annotated-players))
;(def cluster-0 (conj (get-cluster-n 0 annotated-players) cluster-keys))
(def cluster-0 annotated-players)
(def cluster-1 annotated-players)

(matrix cluster-0)




