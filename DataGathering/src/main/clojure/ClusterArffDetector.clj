(ns ClusterArffDetector
  (:require clojure.string))

(filter #(. % contains "cluster") (clojure.string/split (slurp "/tmp/lala.arff") #",|\n"))