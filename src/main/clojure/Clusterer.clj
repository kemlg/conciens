(ns Clusterer
  (:use clj-ml.io)
  (:use clj-ml.clusterers)
  (:use clj-ml.data)
  (:use clj-ml.filters)
  (:require [snippets-generic :as cs])
  (:import (java.io File FileInputStream FileOutputStream)))

(def props (cs/load-props "conciens.properties"))
(def questions-file (str (:local.output_dir props) "/wow-questions.csv"))
(def achievements-file (str (:local.output_dir props) "/wow-achievements.csv"))
(def temp-file "/tmp/clustering.tmp")

(defn get-ids [f]
  (let [data (line-seq (clojure.java.io/reader f))
        ids (map #(first (clojure.string/split % #",")) (drop 1 data))]
    (into [] ids)))

(defn save-clusters [origin clustering]
  (let [data (line-seq (clojure.java.io/reader clustering))
        ids (get-ids origin)
        cf (clojure.string/replace origin #".csv" "-clustered.csv")]
    (spit cf (str "id," (first data) "\n"))
    (dorun
      (map-indexed
        (fn [idx,c]
          (spit cf (str (nth ids idx) "," c "\n") :append true))
        (drop 1 data)))))

(defn assign-clusters [file clustering]
  (let [data (line-seq (clojure.java.io/reader clustering))
        original-data (line-seq (clojure.java.io/reader file))
        destinations (map #(clojure.string/replace file #".csv" (str "." % ".csv")) [0 1])]
    (dorun (map #(spit % (str (first data) "\n")) destinations))
    (dorun (map-indexed
             (fn [idx n]
               (spit
                 (nth destinations (java.lang.Integer/parseInt (last (clojure.string/split (nth (drop 1 data) idx) #","))))
                 (str n "\n")
                 :append true ))
             (drop 1 original-data)))))

(let [raw-ds (load-instances :csv (FileInputStream. (File. questions-file)))
      ds (filter-apply (make-filter :remove-attributes {:dataset-format raw-ds :attributes [0]}) raw-ds)
      kmeans (make-clusterer :k-means {:number-clusters 2})]
  (clusterer-build kmeans ds)
  ; (spit (str (:local.output_dir props) "/clustering.txt") kmeans)
  (let [cds (clusterer-cluster kmeans ds)]
    (save-instances :csv (FileOutputStream. (File. temp-file)) cds)
    (save-clusters questions-file temp-file)
    (assign-clusters achievements-file temp-file)))
