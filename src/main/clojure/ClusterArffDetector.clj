(ns ClusterArffDetector
  (:require clojure.string))

(defn arff-detector []
  (filter #(. % contains "cluster") (clojure.string/split (slurp "/tmp/lala.arff") #",|\n")))
