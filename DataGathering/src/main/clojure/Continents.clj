(ns Continents
  (:require [clojure.tools.logging :as log])
  (:require [clojure.string :as st])
  (require [clojure.contrib.duck-streams])
  (:require [clojure.java.jdbc :as sql])
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

(defn update-db-continents []
  (sql/with-connection 
    db
    (sql/with-query-results rs ["SELECT r.name, r.battlegroup, p.url FROM realms r, players p WHERE r.name = p.realm AND r.battlegroup = p.battlegroup AND r.language = 'EN' GROUP BY r.name, r.battlegroup;"]
      (doall (map set-continent rs)))))






