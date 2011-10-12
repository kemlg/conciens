(ns Stats
  (:use (incanter core stats charts datasets io)))

(def data (read-dataset "/Users/sergio/Desktop/WoW_DB.csv" :header true))
(def iris (to-matrix data))
(def pca (principal-components (sel iris :cols (range 4))))
