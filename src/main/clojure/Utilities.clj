(ns Utilities
  (:require [clojure.xml :as cxml])
  (:require [clojure.tools.logging :as log])
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

(defn get-xml-url [url]
  (cxml/parse 
    (new ByteArrayInputStream 
         (.getBytes
           (html-xml
             (do
               (println "fetching" url)
               (fetch-url url)))))))

