(ns GetCSV
  (:require [clojure.tools.logging :as log])
  (:require [clojure.string :as st])
  (:require [clojure.java.jdbc :as sql])
  (:require  [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import (java.sql DriverManager))
  (:import [org.htmlcleaner HtmlCleaner SimpleXmlSerializer CleanerProperties]
           [org.apache.commons.lang StringEscapeUtils]))

(def db (load-file "./contrib/config.clj"))

(defn get-achievements-names [d]
(sql/with-connection d
   (sql/with-query-results rs ["select distinct(id), name from achievements order by id ASC"] 
     ; rs will be a sequence of maps, 
     ; one for each record in the result set. 
     (doall (map #(str "\"" (. (:name %) replaceAll "\"" "\\\"") "\"") rs)))))

(defn get-achievements [d]
(sql/with-connection d
   (sql/with-query-results rs ["select distinct(id) from achievements order by id ASC"] 
     ; rs will be a sequence of maps, 
     ; one for each record in the result set. 
     (doall (map #(:id %) rs)))))

(defn get-achievements-player [d id]
  (sql/with-connection d
   (sql/with-query-results rs 
     ["select achievement_id as id from achievements_players where player_id=? order by achievement_id asc" id]
     (println "lala") 
      (doall (map #(:id %) rs))
      )))

(defn set-value [ach a]
  (contains? ach a))

(defn get-player [d ach id]
  (def ap (get-achievements-player d id))
  (map #(set-value ap %) ach))

;(map #(get-player db achs (:id %)) 
 ;    (get-players db))

;(println "Finished!")

(defn get-achievements-players []
  (sql/with-connection db
   (sql/with-query-results rs 
     ["select a.id, ap.player_id from achievements_players ap, achievements a where a.name = ap.achievement order by ap.player_id asc, a.id asc;"]
      (doall (map #(hash-map :player (:player_id %) :achievement (:id %)) rs))
      )))

(defn cross-achievements [origin candidate]
  (if (. origin contains candidate)
    1
    0))

(defn build-player [x player achs]
  (def mat (map #(:achievement %) x))
  ;(duck/append-spit "/tmp/lala.csv"
  ;  (apply str (doall (interpose "," (concat 
  ;  (list (:id player) (:name player) (:race player) (:class player) (:type player) (:language player) (:population player) (:realm player) (count mat))
  ;  (map (fn [y] (cross-achievements mat y)) achs))))))
  ;  (duck/append-spit "/tmp/lala.csv" "\n"))
  (apply merge (sorted-map) player (map (fn [y] {(keyword (str "z" y)) (cross-achievements mat y)}) achs)))
  
(defn preparar-registro [rs]
  ;:id (:id %) :name (:name %) :race (:race %) :class (:class %) :type (:type %) :language (:language %) :population (:population %) :realm (:realm %) :mapa {}
  (def row (into {} rs))
  (dissoc (dissoc (dissoc row :id_distinct) :url) :complete))

(defn get-players [d]
  (sql/with-connection d
    (sql/with-query-results
      rs
      [
        "select distinct(p.id) as id_distinct, p.*, l.population as population, l.language as language, l.type as type from realms l, players p, achievements_players ap where p.id = ap.player_id and p.realm = l.name and p.battlegroup = l.battlegroup;"
      ]
      (doall (map #(preparar-registro %) rs)))))

(defn get-csv [filename]
  ;(duck/spit "/tmp/lala.csv"
  ;         (apply str (doall (interpose ","
  ;                    (concat (list "id" "name" "race" "class"  "type" "language" "population" "realm" "num") (get-achievements-names db))))))
  ;(duck/append-spit "/tmp/lala.csv" "\n")
  (def achs (get-achievements db))
  (def players (get-players db))
  ;(get-achievements db)
  (def ap (get-achievements-players))
  (def map-total (map #(build-player (filter (fn [x] (= (:player x) (:id %))) ap) % achs) players)) 
  ;(first map-total))
  (with-open [out-file (io/writer filename)]
    (csv/write-csv out-file (cons (map name (keys (first map-total))) (map #(vals %) map-total)))))

(get-csv "/Users/sergio/Desktop/players.csv")
