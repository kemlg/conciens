(ns Stats
  (:use (incanter core stats charts datasets io)))

(def data (read-dataset "/Users/sergio/Desktop/players.csv" :header true :delim \;))
;(def iris (to-matrix data))
;(def pca (principal-components (sel iris :cols (range 4))))

(get-categories :level data)
(println (second data))