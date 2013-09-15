(ns Clusterer
  (:use clj-ml.io)
  (:use clj-ml.clusterers)
  (:use clj-ml.filters)
  (:require [snippets-generic :as cs])
  (:import (java.io File FileInputStream)))

(def props (cs/load-props "conciens.properties"))

(let [raw-ds (load-instances :csv (FileInputStream. (File. (str (:local.output_dir props) "/wow-questions.csv"))))
      ds (filter-apply (make-filter :remove-attributes {:dataset-format raw-ds :attributes [0]}) raw-ds)
      kmeans (make-clusterer :k-means {:number-clusters 2})]
  (clusterer-build kmeans ds)
  (spit (str (:local.output_dir props) "/clustering.txt") kmeans))

