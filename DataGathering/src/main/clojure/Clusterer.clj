(ns Clusterer
  (:use clj-ml.io)
  (:use clj-ml.clusterers)
  (:use clj-ml.filters)
  (:require [snippets-generic :as cs])
  (:import (java.io File FileInputStream FileOutputStream)))

(def props (cs/load-props "conciens.properties"))

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

(let [raw-ds (load-instances :csv (FileInputStream. (File. (str (:local.output_dir props) "/wow-questions.csv"))))
      ds (filter-apply (make-filter :remove-attributes {:dataset-format raw-ds :attributes [0]}) raw-ds)
      kmeans (make-clusterer :k-means {:number-clusters 2})]
  (clusterer-build kmeans ds)
  (spit (str (:local.output_dir props) "/clustering.txt") kmeans)
  (let [cds (clusterer-cluster kmeans ds)]
    (save-instances :csv (FileOutputStream. (File. "/tmp/clustering.tmp")) cds)
    (save-clusters (str (:local.output_dir props) "/wow-questions.csv") "/tmp/clustering.tmp")))
