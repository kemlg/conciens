(ns Armory
  (:require [clojure.contrib.logging :as log])
  (:require [clojure.contrib.string :as st])
  (require [clojure.contrib.duck-streams])
  (:use [clojure.contrib.sql] )
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

(defn get-body [s]
  (filter #(= :body (get % :tag)) (:content (first s))) )

(defn get-data [s]
  (map #(get % :tag) (:content (first (get-body s)))))

(defn find-map [text]
  (+ 20 (. text indexOf "hideCount: 1, data:")))

(defn find-end [text]
  (- (. text indexOf ";myTabs.flush()") 2))

(defn extract-json [text]
  (. text substring 
    (find-map text)
    (find-end text)))

(defn extract-fields [txt idx]
  (def a (. txt indexOf "'" idx))
  (def b (. txt indexOf "'" (+ 1 a)))
  (def c (+ 11 (. txt indexOf ",realmname:'" idx)))
  (def d (. txt indexOf "',battlegroup:" idx))
  (list (. txt substring (+ 1 a) b) (. txt substring (+ 1 c) d)))

(defn get-map [json]
(map #(extract-fields json %)
(loop [ct (. json indexOf ",name:") s nil]
  (if (= ct -1)
    (seq s)
    (do
      (recur (. json indexOf ",name:" (+ 1 ct)) (cons ct s)))))))

(defn create-url [nombre reino apartado codigo]
  (str
    "http://eu.battle.net/wow/en/character/"
    (. (. reino toLowerCase) replaceAll " " "-")
    "/"
    (. nombre toLowerCase)
    "/"
    apartado
    "/"
    codigo))

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
  (create-url (first lista) (second lista) apartado codigo))

; (apply bajar-url (first mapa) "achievement" achi)

(defn get-list [txt]
  (:content (second (rest (:content (first (:content (first (second (second (rest (second (:content txt)))))))))))))

(defn extract-achievement [li]
  (if (not (. (:class (:attrs li)) contains "locked"))
    (do
      (first (:content (first (:content (first (:content li)))))))))

;(map #((clojure.xml/parse (new ByteArrayInputStream (.getBytes (html-xml (fetch-url %))))))

(defn descarga [url]
  (try
  (extract-achievement
    (first
      (get-list
        (clojure.xml/parse 
          (new ByteArrayInputStream 
             (.getBytes
               (html-xml
                 (do
;                   (println "fetching" url)
                   (fetch-url url)))))))))
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
  ;(println ach)
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
    (dorun (map #(insert-achievement (:id (first rs)) %) (filter notnil? (map descarga (map #(bajar-url p "achievement" %) achi))))))
  (catch Exception e (println e))))

(defn store-in [x cl race]
(println x cl race)
(try
   (execute-player x cl race)
   (catch Exception e
     (println "Error when parsing" e))))

(defn mapa [txt]
(get-map (extract-json
  (fetch-url txt))))

(defn cartesian-product
  "All the ways to take one item from each sequence"
  [& seqs]
  (let [v-original-seqs (vec seqs)
	step
	(fn step [v-seqs]
	  (let [increment
		(fn [v-seqs]
		  (loop [i (dec (count v-seqs)), v-seqs v-seqs]
		    (if (= i -1) nil
			(if-let [rst (next (v-seqs i))]
			  (assoc v-seqs i rst)
			  (recur (dec i) (assoc v-seqs i (v-original-seqs i)))))))]
	    (when v-seqs
	       (cons (map first v-seqs)
		     (lazy-seq (step (increment v-seqs)))))))]
    (when (every? first seqs)
      (lazy-seq (step v-original-seqs)))))

(def vlimits (vector 0 50 100 150))
(def vclasses (vector 3 4 5 6 7 8 9 11 1 2)) ;-- Falta 1
(def vraces (vector 3 4 5 6 7 8 9 10 11 22 1 2)) ;-- Falta 1

(defn get-wowhead [limit cl race]
  (println limit cl race)
  (let [url (str "http://www.wowhead.com/profiles=eu?filter=cl=" cl ";ra=" race ";minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:" limit)]
    (println "Reading " url)
    (try
    (dorun (map #(store-in % cl race) (mapa url)))
    (catch Exception e (println e)))))

;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:0"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:50"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:100"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:150"))

(def combs (map flatten (cartesian-product (cartesian-product vlimits vclasses) vraces)))

(defn run-thread [a b c]
  (future (get-wowhead a b c)))

(clojure.contrib.sql/with-connection
  db
  (dorun (map #(apply run-thread %) combs)))
