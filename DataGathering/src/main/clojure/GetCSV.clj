(ns GetCSV
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.contrib.string :as st])
  (require [clojure.contrib.duck-streams :as duck])
  (:use [clojure.contrib.sql])
  (:require [clojure.contrib.sql :as sql])
  (:import (java.sql DriverManager))
  (:import [org.htmlcleaner HtmlCleaner SimpleXmlSerializer CleanerProperties]
           [org.apache.commons.lang StringEscapeUtils]))

(def db (load-file "./contrib/config.clj"))

(defn get-achievements-names [d]
(with-connection d
   (with-query-results rs ["select distinct(id), name from achievements order by id ASC"] 
     ; rs will be a sequence of maps, 
     ; one for each record in the result set. 
     (doall (map #(str "\"" (. (:name %) replaceAll "\"" "\\\"") "\"") rs)))))

(defn get-achievements [d]
(with-connection d
   (with-query-results rs ["select distinct(id) from achievements order by id ASC"] 
     ; rs will be a sequence of maps, 
     ; one for each record in the result set. 
     (doall (map #(:id %) rs)))))

(defn get-achievements-player [d id]
  (with-connection d
   (with-query-results rs 
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
  (with-connection db
   (with-query-results rs 
     ["select * from achievements_players order by player_id asc, achievement_id asc"]
      (doall (map #(hash-map :player (:player_id %) :achievement (:achievement_id %)) rs))
      )))

(defn cross-achievements [origin candidate]
  (. origin contains candidate))

(defn build-player [x player achs]
  (def mat (map #(:achievement %) x))
  (duck/append-spit "/tmp/lala.csv"
    (apply str (doall (interpose "," (concat 
    (list (:id player) (:name player) (:race player) (:class player) (:type player) (:language player) (:population player) (:realm player) (count mat))
    (map (fn [y] (cross-achievements mat y)) achs))))))
    (duck/append-spit "/tmp/lala.csv" "\n"))

(defn get-players [d]
  (with-connection d
    (with-query-results rs ["select distinct(p.id) as id, p.name as name, r.name as race, c.name as class, l.name as realm, l.population as population, l.language as language, l.type as type from realms l, players p, achievements_players ap, races r, classes c where p.id = ap.player_id and r.id = p.id_race and c.id = p.id_class and p.realm = l.name"]
      (doall 
        (map #(hash-map 
                :id (:id %) :name (:name %) :race (:race %) :class (:class %) :type (:type %) :language (:language %) :population (:population %) :realm (:realm %) :mapa {}) 
             rs)))))

(defn get-csv []
  (duck/spit "/tmp/lala.csv"
           (apply str (doall (interpose ","
                      (concat (list "id" "name" "race" "class"  "type" "language" "population" "realm" "num") (get-achievements-names db))))))
  (duck/append-spit "/tmp/lala.csv" "\n")
  (def achs (get-achievements db))
  (def players (get-players db))
  ;(get-achievements db)
  (def ap (get-achievements-players))
  (dorun (map #(build-player (filter (fn [x] (= (:player x) (:id %))) ap) % achs) players)))


