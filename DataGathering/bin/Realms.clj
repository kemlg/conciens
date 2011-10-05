(ns Armory
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

(defn fetch-url
  "Return the web page as a string."
  [address]
  (let [url (URL. address)]
    (with-open [stream (. url (openStream))]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
        (apply str (line-seq buf))))))

(defn html-xml
  "Given the HTML source of a web page, parses it and returns the :title
   and the tag-stripped :content of the page. Does not do any encoding
   detection, it is expected that this has already been done."
  [page-src]
  (try
   (when page-src
     (let [cleaner (new HtmlCleaner)]
       (doto (.getProperties cleaner) ;; set HtmlCleaner properties
         (.setOmitComments true)
         (.setPruneTags "script,style"))
       (. (new SimpleXmlSerializer (. cleaner getProperties)) getAsString (.clean cleaner page-src))))
   (catch Exception e
     (log/error "Error when parsing" e))))

(defn get-realms []
(clojure.xml/parse (new ByteArrayInputStream (.getBytes (html-xml
   (fetch-url "http://eu.battle.net/wow/en/status")
))))
)

(defn get-list [x]
  (:content (second (:content (first (:content (first (:content (nth (:content (second (:content (first (:content (second (:content (first (:content (second (:content x))))))))))) 3))))))))
)

(defn get-info [x]
  {
    :realm (st/trim (first (:content (second (:content x)))))
    :type (:data-raw (:attrs (nth (:content x) 2)))
    :population (:data-raw (:attrs (nth (:content x) 3)))
    :language (. (st/trim (first (:content (nth (:content x) 4)))) toLowerCase)
  }
)

(defn get-list-realms []
  (map get-info (pop (get-list (get-realms))))
)

(defn update-record [m]
(sql/with-connection db
  (sql/update-values
    :realms
    ["name=?" (:realm m)]
    (dissoc m :realm)
  )
)
)

(map update-record
     (get-list-realms))

;; (println "!!!!!!!!!!!!!!!!!!!! FINISHED !!!!!!!!!!!!!!!!!!!!")









