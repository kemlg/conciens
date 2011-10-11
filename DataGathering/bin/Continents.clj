(ns Continents
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.contrib.string :as st])
  (require [clojure.contrib.duck-streams])
  (:use [clojure.contrib.sql])
  (:require [clojure.contrib.sql :as sql])
  (:import (java.sql DriverManager))
  (:import [org.htmlcleaner HtmlCleaner SimpleXmlSerializer CleanerProperties]
           [org.apache.commons.lang StringEscapeUtils]))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader ByteArrayInputStream))

(def db (load-file "./contrib/config.clj"))

(defn set-continent [row]
  (def continent (. (:url row) substring 7 9))
  (sql/with-connection
    db
    (sql/update-values
      :realms
      ["name=? and battlegroup=?" (:name row) (:battlegroup row)]
      {:continent continent})))

(sql/with-connection 
  db
  (with-query-results rs ["SELECT r.name, r.battlegroup, p.url FROM realms r, players p WHERE r.name = p.realm AND r.battlegroup = p.battlegroup GROUP BY r.name, r.battlegroup;"]
    (doall (map set-continent rs))))






