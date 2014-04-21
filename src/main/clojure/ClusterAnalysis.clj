(ns ClusterAnalysis
  (:use MahoutTest)
  (:use (incanter core stats datasets charts))
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn merge-2-rows[a b]
  (doall (merge-with concat a b)))

(defn merge-matches [m]
  (doall (reduce merge-2-rows m)))

(defn numerize [s]
  (try (if (string? s) (if (number? (read-string s)) (read-string s) s) s) (catch Exception e s)))

(defn vector-num [s]
  (let [n (numerize s)]
    (vector n)))

(defn convert-values-to-lists [r]
  (into {} 
    (for [[k v] r] 
      [k (vector-num v)])))

(defn get-all-values [m idx]
  (map #(nth % idx) m))

(defn transpose [mtx]
  (let [idxs (range (count (first mtx)))]
    (map #(get-all-values mtx %) idxs)))

(defn columna-interesante [r]
  (let [a (key r)]
    (or
      (and (number? a) (>= a 999900))
      (= a "id")
      (= a ":cluster")
      (= a :cluster)
      (= a ":cluster0"))))

(defn cambia-nombres [r]
  (if
    (and (number? (key r)) (>= (key r) 999900))
    (vector (Mongo2CSV/questions-map (key r)) (val r))
    r))

(defn write-clusters-to-csv [nm m]
  (dorun
    (let [mapa-clusters m]
      (let [mapa-clusters-lists (map convert-values-to-lists mapa-clusters)]
          (let [big-matrix (merge-matches mapa-clusters-lists)]
            (let [medium-matrix (into {} (map cambia-nombres (filter columna-interesante big-matrix)))]
              (with-open [f (io/writer (str "/tmp/" nm ".csv"))]
                (csv/write-csv f (cons (keys medium-matrix) (transpose (vals medium-matrix)))))))))))

(defn cluster-and-store []
  (let [annotated-players ((memoize annotate-clusters) 3 ((memoize Mongo2CSV/get-all-data)))]
    (let [mapa-clusters (map #(apply hash-map (flatten %)) annotated-players)]
      (write-clusters-to-csv "clustering" annotated-players))))

;mapa-clusters-lists

(defn read-clusters-from-csv []
  (with-open [f (io/reader "/tmp/clustering.csv")]
    (doall (csv/read-csv f))))

(defn numerize-vector [v]
  (map numerize v))

(defn get-mapeo [ks vs]
  (let [idxs (range (count ks))]
    (apply hash-map (flatten (map #(vector (nth ks %) (nth vs %)) idxs)))))

;(cluster-and-store)
(defn process-csv []
  (let [mapa-tonto (map numerize-vector (read-clusters-from-csv))]
    (let [claves (first mapa-tonto) valores (rest mapa-tonto)]
      (map #(get-mapeo claves %) valores))))

(defn sum-if-number [a b]
  (if (number? a) (+ a b) nil))

(defn merge-with-sum [a b]
  (merge-with sum-if-number a b))

(defn calculate-means [h n]
  (vector (key h) (if (number? (val h)) (double (/ (val h) n)) nil)))

(defn give-means [m]
  (into {} (map #(calculate-means % (count m)) (reduce merge-with-sum m))))

(defn statistics [cluster]
  (into {} (filter #(let [k (key %)] (and (number? k) (>= k 999900))) cluster)))

(defn replace-cluster-key [m]
  (assoc (dissoc m ":cluster") ":cluster0" (get m ":cluster")))

(defn write-clusters-to-csv-indexed [idx cl]
  (write-clusters-to-csv (str "cluster" idx) cl))

(defn double-clustering []
  (let [pc (sort (fn [a b] (compare (get a ":cluster") (get b ":cluster"))) (process-csv))]
    (let [clusters (partition-by #(get % ":cluster") pc)]
      (let
        [clusters-rdy
         (map
           #(map replace-cluster-key %)
           clusters)]
        (let [four-clusters (map annotate-clusters clusters-rdy)]
          (map-indexed write-clusters-to-csv-indexed four-clusters))))))

(cluster-and-store)
