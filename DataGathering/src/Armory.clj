(ns Armory
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.contrib.string :as st])
  (require [clojure.contrib.duck-streams])
  (:use [clojure.contrib.sql])
  (:use Utilities)
  (:require [clojure.contrib.sql :as sql])
  (:import (java.sql DriverManager))
  (:import [org.htmlcleaner HtmlCleaner SimpleXmlSerializer CleanerProperties]
           [org.apache.commons.lang StringEscapeUtils]))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader ByteArrayInputStream))

(defn get-body [s]
  (filter #(= :body (get % :tag)) (:content (first s))) )

(defn get-data [s]
  (map #(get % :tag) (:content (first (get-body s)))))

(defn create-base-url [nombre reino]
  (str
    "http://eu.battle.net/wow/en/character/"
    (. (. reino toLowerCase) replaceAll " " "-")
    "/"
    (. nombre toLowerCase)))

(defn create-url [base apartado codigo]
  (str base "/" apartado "/" codigo))

(def achi (vector 92
                96 14861 15081 14862 14863 15070
                97 14777 14778 14779 14780 15069
                95 165 14801 14802 14803 14804 14881 14901 15003 15073 15074 15075 15092
                168 14808 14805 14806 14922 15067 15068
                169 170 171 172 15071
                201 14864 14865 14866 15072
                155 160 187 159 163 161 162 158 14981 156 14941))
                ;; Dejamos los feats of strength fuera: 81))

(defn bajar-url [lista apartado codigo]
  (create-url (create-base-url (first lista) (second lista)) apartado codigo))

; (apply bajar-url (first mapa) "achievement" achi)

(defn get-list [txt]
  (:content (second (rest (:content (first (:content (first (second (second (rest (second (:content txt)))))))))))))

(defn extract-achievement [li]
  ;(println li)
  (if (not (. (:class (:attrs li)) contains "locked"))
    (do
      (first (:content (first (:content (first (:content li)))))))))

;(map #((clojure.xml/parse (new ByteArrayInputStream (.getBytes (html-xml (fetch-url %))))))

(defn descarga [url]
  (try
  (map extract-achievement
    (first
      (get-list
        (get-xml-url url)
    )
    ))
  (catch NullPointerException e (println url)))
)

(defn notnil? [x]
  (not (nil? x)))

(defn insert-player
  [nombre reino cl race]
  (clojure.contrib.sql/insert-values
   :players
   [:name :realm :id_class :id_race]
   [nombre (. reino replaceAll "'" "\\'") cl race]))

(defn insert-achievement
  [id,ach]
  (println ach)
  (try
  (clojure.contrib.sql/insert-values
   :achievements
   [:name]
   [(. ach replaceAll "'" "\\'")])
    (catch Exception e))
  (println (str "select id from achievements where name = '" (. ach replaceAll "'" "''") "'"))
  (with-query-results rs [(str "select id from achievements where name = '" (. ach replaceAll "'" "''") "'")]
    (try
    (clojure.contrib.sql/insert-values
      :achievements_players
      [:achievement_id :player_id]
      [(:id (first rs)) id])
    (catch Exception e))))

(defn execute-player [p cl race]
  (println p cl race)
  (try
  (insert-player (first p) (second p) cl race)
  (catch Exception e (println e)))
  (try
  (println (str "select id from players where name = '" (first p) "' and realm = '" (second p) "'"))
  (with-query-results rs [(str "select id from players where name = '" (first p) "' and realm = '" (second p) "'")] 
    (dorun (map #(insert-achievement (:id (first rs)) %) (filter notnil? (flatten (map descarga (map #(bajar-url p "achievement" %) achi)))))))
  (catch Exception e (println e))))





