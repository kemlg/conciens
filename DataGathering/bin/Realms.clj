(ns Armory
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.contrib.string :as st])
  (require [clojure.contrib.duck-streams])
  (:use Utilities)
  (:use [clojure.contrib.sql])
  (:require [clojure.contrib.sql :as sql])
  (:import (java.sql DriverManager))
  (:import [org.htmlcleaner HtmlCleaner SimpleXmlSerializer CleanerProperties]
           [org.apache.commons.lang StringEscapeUtils]))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader ByteArrayInputStream))

(defn get-realms [continent]
  (clojure.xml/parse (new ByteArrayInputStream (.getBytes (html-xml
    (fetch-url (str "http://" continent ".battle.net/wow/en/status")))))))

(defn get-list [x]
  (:content (second (:content (first (:content (first (:content (nth (:content (second (:content (first (:content (second (:content (first (:content (second (:content x))))))))))) 3)))))))))

(defn get-info [x]
  {
    :realm (st/trim (first (:content (second (:content x)))))
    :type (:data-raw (:attrs (nth (:content x) 2)))
    :population (:data-raw (:attrs (nth (:content x) 3)))
    :language (. (st/trim (first (:content (nth (:content x) 4)))) toLowerCase)})

(defn get-list-realms [continent]
  (map get-info (pop (get-list (get-realms continent)))))

(defn update-record [continent m]
  (sql/with-connection db
    (sql/update-values
      :realms
      ["name=? and continent=? and language='EN'" (:realm m) continent]
      (dissoc m :realm))))

(defn update-realms [continent]
  (map #(update-record continent %)
    (get-list-realms continent)))

(sql/with-connection
  db
  (with-query-results rs ["SELECT DISTINCT(continent) FROM realms;"]
    (doall (map #(update-realms (:continent %)) rs))))
                  
  
;; (println "!!!!!!!!!!!!!!!!!!!! FINISHED !!!!!!!!!!!!!!!!!!!!")









