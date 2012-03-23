(ns MahoutTest
  (:import [org.apache.mahout.clustering.kmeans KMeansClusterer Cluster])
  (:import [org.apache.mahout.common.distance EuclideanDistanceMeasure])
  (:import [org.apache.mahout.math DenseVector])
  (:require Mongo2CSV))

(def measure (EuclideanDistanceMeasure.))

(defn is-a-question? [n]
  (let [idx (first n)] (and (number? idx) (>= idx 999900))))

(defn is-an-achievement? [n]
  (let [idx (first n)] (and (number? idx) (< idx 999900))))

(defn get-vector [p]
  (map second (filter is-an-achievement? p)))

(defn get-question-vector [p]
  (map #(. Integer parseInt (second %)) (filter is-a-question? p)))

(defn get-all-vectors [ps]
  (map get-vector (take 212 ps)))

(defn get-point-vectors [players]
  (map #(DenseVector. (double-array %)) (get-all-vectors players)))

(defn create-random-clusters [points]
  (take 2 (map-indexed (fn [idx v] (Cluster. (DenseVector. (double-array (map eval (take (. v size) (repeat '(rand-int 1)))))) idx measure)) points)))

(defn clusterize [players]
  (def points (get-point-vectors players))
  (def clusters (create-random-clusters points))
  (loop [cnt 15 cls clusters]
    (if (zero? cnt)
      cls
      (recur (dec cnt) (last (. KMeansClusterer clusterPoints points cls measure 100 999))))))

(defn get-distances [v cs]
  (map #(. v getDistanceSquared (. % getCenter)) cs))

(defn get-cluster [p cs]
  (def s (get-distances (DenseVector. (double-array (get-vector p))) cs))
  (if (< (first s) (second s)) 0 1))

(comment "validation"
(defn get-number [st] (if (. st equals "cluster0") 0 1))
(def wekas (map get-number (filter #(. % contains "cluster") (clojure.string/split (slurp "/tmp/lala.arff") #",|\n"))))

(def mahouts (map #(get-cluster % classes) all-players))

(def comparacion (map #(= (first %) (second %)) (partition 2 (interleave wekas mahouts))))
(println (count wekas) " " (count mahouts))
(println (count (filter #(= % true) comparacion))))

(defn annotate-clusters [ps]
  (let [classes (clusterize ps)]
    (map #(conj % [:cluster (get-cluster % classes)]) ps)))

(defn get-cluster-n [idx ps]
  (let [everything (filter #(= idx (second (first %))) ps)]
    (map get-question-vector everything)))

(defn get-cluster-keys [ps]
  (map first (filter is-a-question? (first ps))))



