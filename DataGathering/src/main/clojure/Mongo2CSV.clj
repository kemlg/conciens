(ns Mongo2CSV
    (:require [somnium.congomongo :as cm])
    (:use [clojure.set])
    (:require [clojure.data.csv :as csv]
              [clojure.java.io :as io]))

(def conn (load-file "./contrib/mongodb.clj"))

(defn extract-info [row]
  (hash-map :id (row (keyword "Main Character Profile URL")) :questions (row :questions) :achievements ((row :achievements) :achievementsCompleted)))

(defn get-row-achievements [row]
  (apply sorted-set (row :achievements)))

(defn get-all-achievements [s]
  (apply union (map get-row-achievements s)))

(defn process-row [row achi]
  (def table-achievements (apply sorted-map (flatten (map #(list % (if (contains? (row :achievements) %) 1 0)) achi))))
  (apply concat (list (hash-map :id (row :id)) (row :questions) table-achievements)))

(def extracted-data (map extract-info (cm/with-mongo conn (cm/fetch :players))))
(def all-achievements (get-all-achievements extracted-data))

(def all-data (map #(process-row % all-achievements) extracted-data))

(defn get-firsts [row]
  (map #(get % 1) row))

(with-open [out-file (io/writer "/tmp/wow-data.csv")]
  (csv/write-csv
    out-file
    (cons (map #(get % 0) (first all-data))
          (map get-firsts all-data))))


