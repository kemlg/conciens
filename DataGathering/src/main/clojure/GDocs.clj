(ns GDocs
  (:use Armory)
  (:require [clojure.java.jdbc :as sql])
  (:require [clojure.string :as st])
  (:use Utilities)
  (:require [clojure.pprint :as ccpp])
  (:use clojure.stacktrace)
  (:use ParseExcelDocs))

(defn get-character-info [xml]
  (def profile (:content (first (:content (first (:content (first (:content (first (:content (first (:content (second (:content (first (:content (second (:content (first (:content (second (:content xml))))))))))))))))))))))
  (hash-map :cabecera (first profile) :datos (second profile)))

(defn get-player-data [p base map-profile]
  (def battlegroup (:data-battlegroup (:attrs (last (:content (nth (:content (first (:content (:cabecera map-profile)))) 3))))))
  (def clean-url (:href (:attrs (first (:content (first (:content (first (:content (:cabecera map-profile))))))))))
  (def char-name (st/trim (first (:content (first (:content (first (:content (first (:content (:cabecera map-profile)))))))))))
  (def char-title (st/trim (first (:content (first (:content (second (:content (first (:content (:cabecera map-profile)))))))))))
  (try
    (def guild-name (st/trim (first (:content (first (:content (second (:content (second (:content (first (:content (:cabecera map-profile)))))))))))))
    (catch Exception e
      (def guild-name "none")))
  (def char-achievements (read-string (st/trim (first (:content (first (:content (nth (:content (first (:content (:cabecera map-profile)))) 4))))))))
  (def char-race (st/trim (first (:content (second (:content (nth (:content (first (:content (:cabecera map-profile)))) 3)))))))
  (def char-level (read-string (st/trim (first (:content (first (:content (first (:content (nth (:content (first (:content (:cabecera map-profile)))) 3))))))))))
  (def char-class (st/trim (first (:content (nth (:content (nth (:content (first (:content (:cabecera map-profile)))) 3)) 3)))))
  (def char-spec (st/trim (first (:content (nth (:content (nth (:content (first (:content (:cabecera map-profile)))) 3)) 2)))))
  (def char-realm (st/trim (first (:content (last (:content (nth (:content (first (:content (:cabecera map-profile)))) 3)))))))
  (hash-map
    :battlegroup battlegroup
    :name char-name
    :url (str "http://" base clean-url)
    :spec char-spec
    :title char-title
    :guild guild-name
    :race char-race
    :level char-level
    :class char-class
    :points char-achievements
    :realm char-realm
    :advancement (read-string (get p "advancement"))
    :mechanics (read-string (get p "mechanics"))
    :competition (read-string (get p "competition"))
    :socializing (read-string (get p "socializing"))
    :relationship (read-string (get p "relationship"))
    :teamwork (read-string (get p "teamwork"))
    :discovery (read-string (get p "discovery"))
    :roleplaying (read-string (get p "roleplaying"))
    :customization (read-string (get p "customization"))
    :escapism (read-string (get p "escapism"))
    :achievement (read-string (get p "achievement"))
    :social (read-string (get p "social"))
    :immersion (read-string (get p "immersion"))
    :email (get p "email address")
    :gender (get p "gender")
    :age (let [age (read-string (get p "age"))] (if (number? age) age nil))))

; Advancement	Mechanics	Competition
; Socializing	Relationship	Teamwork
; Discovery	Role-Playing	Customization
; Escapism	Achievement	Social	Immersion

(defn get-clean-url [p base xml]
  (get-player-data p base (get-character-info xml)))

(defn get-base-url [url]
  (nth (st/split url #"\/") 2))

(defn set-english [url]
  (def ss (st/split url #"\/"))
  (def res (apply str (interpose "/" (concat (list "http:/" (nth ss 2) (nth ss 3) "en") (drop 5 ss)))))
  (if (or (. res endsWith "simple") (. res endsWith "advanced"))
    res
    (str res "/")))

(defn get-player [player]
  (try
    (def url (set-english (get player "main character profile url")))
    (get-clean-url player (get-base-url url) (get-xml-url url))
    (catch Exception e
      (print-stack-trace e)
      nil)))

(defn get-values-sheet [sh]
  (map #(:value %) sh))

(defn get-survey-raw [wb]
  (hash-map
    :inputs (map get-values-sheet (:content (first (:content (first wb)))))
    :comps (map get-values-sheet (:content (nth (:content (first wb)) 2)))
    :answers (map get-values-sheet (:content (nth (:content (first wb)) 3)))))

(defn get-survey-concat [wb]
  (def survey (get-survey-raw wb))
  (doall (map #(concat (nth (:inputs survey) %) (nth (:comps survey) %)) (range (count (:comps survey))))))

(defn posicion [s p]
  (if (>= p (count s))
    nil
    (nth s p)))

(defn put-indexes [indexes row]
  (apply hash-map (flatten (map #(list (nth indexes %) (posicion row %)) (range (count indexes))))))

(defn get-survey [wb]
  (def survey (get-survey-concat wb))
  (def indexes (filter #(not (= % "unused")) (map st/lower-case (first survey))))
  (map #(put-indexes indexes %) (rest survey)))

(defn process-character [row]
  ;(println (class row))
  ;(ccpp/pprint row)
  (sql/with-connection
    db
    (execute-player row))
  (:name row))

(defn get-characters [data]
  (map get-player (filter #(not (nil? (get % "main character profile url"))) data)))

(defn process-character-multithread [row]
  (future (process-character row)))

(defn parallelize [s]
  (pmap process-character s))

(defn update-db-gdocs []
  (dorun (map println (map parallelize (partition 4 (shuffle (filter #(not (nil? %)) (get-characters (get-survey (google-docs-treatment))))))))))

(defn get-all-characters []
  (get-characters (get-survey (google-docs-treatment))))











