(ns Mongo2CSV
    (:require [somnium.congomongo :as cm])
    (:use [clojure.set])
    (:require [clojure.data.csv :as csv]
              [clojure.java.io :as io]))

(def conn (load-file "./contrib/mongodb.clj"))

(def map-questions
  (hash-map
    "1) Helping other players-" 999924
    "7) How often do you play to relax from the day's work?" 999940
    "4) How often do you make up stories and histories for your characters?" 999937 
    "How interested are you in the precise numbers and percentages underlying the game mechanics?" 999901 
    "How much do you enjoy exploring the world just for the sake of exploring it?" 999913
    "5) How often do you role-play your character?" 999938
    "6) How often do you play so you can avoid thinking about some of your real-life problems or worries?" 999939
    "How important is it to you that your character is as optimized as possible for their profession / role?" 999902 
    " role?" 999902
    "7) Being immersed in a fantasy world-" 999922
    "4) Accumulating resources, items or money-" 999919
    "10) Doing things that annoy other players-" 999933
    "1) How often do you find yourself having meaningful conversations with other players?" 999934
    "4) Competing with other players-" 999927
    "Would you rather be grouped or soloing?" 999904
    "2) Getting to know other players-" 999925
    "How much do you enjoy working with others in a group?" 999906
    "3) How often have your online friends offered you support when you had a real life problem?" 999936
    "5) Dominating/killing other players-" 999928
    "killing other players-" 999928
    "9) Trying out new roles and personalities with your characters-" 999932 
    "3) Chatting with other players-" 999926
    "How often do you use a character builder or a template to plan out your character's advancement at an early level?" 999903
    "How much do you enjoy finding quests, NPCs or locations that most people do not know about?" 999914
    "1) Leveling up your character as fast as possible-" 999916
    "How important is it to you that your character can solo well?" 999905
    "How much do you enjoy leading a group?" 999907
    "2) How often do you talk to your online friends about your personal issues?" 999935
    "2) Acquiring rare items that most players will never have-" 999917
    "How much do you enjoy collecting distinctive objects or clothing that have no functional value in the game?" 999915 
    "8) Being part of a serious, raid/loot-oriented guild-" 999931
    "loot-oriented guild-" 999931
    "How important is it to you that your character's armor / outfit matches in color and style?" 999911 
    " outfit matches in color and style?" 999911
    "6) Having a self-sufficient character-" 999921
    "How often do you take charge of things when grouped?" 999908
    "8) How often do you purposefully try to provoke or irritate other players?" 999941
    "8) Escaping from the real world-" 999923
    "5) Knowing as much about the game mechanics and rules as possible-" 999920
    "3) Becoming powerful-" 999918
    "How important is it to you to be well-known in the game?" 999909
    "6) Exploring every map or zone in the world-" 999929
    "7) Being part of a friendly, casual guild-" 999930
    "How much time do you spend customizing your character during character creation?" 999910
    "How important is it to you that your character looks different from other characters?" 999912
    ))

(defn strings-to-ids [row]
  (def idd (get map-questions (name (key row))))
  (sorted-map (if (nil? idd) (name (key row)) idd) (val row)))

(defn normalize-name [txt]
  (. txt replaceAll "%" "PERCENT"))

(defn extract-info [row]
  (hash-map :id (normalize-name (row (keyword "Main Character Profile URL"))) :questions (apply concat (map strings-to-ids (row :questions))) :achievements ((row :achievements) :achievementsCompleted)))

(defn get-row-achievements [row]
  (apply sorted-set (row :achievements)))

(defn get-all-achievements [s]
  (apply union (map get-row-achievements s)))

(defn process-row [row achi]
  (def vs (apply sorted-set (row :achievements)))
  (def table-achievements (apply concat (map #(apply hash-map (list % (if (contains? vs %) 1 0))) achi)))
  (apply concat (list (hash-map "id" (row :id)) (row :questions) table-achievements)))

(def extracted-data (map extract-info (cm/with-mongo conn (cm/fetch :players))))
(def all-achievements (get-all-achievements extracted-data))

(def all-data (map #(process-row % all-achievements) extracted-data))

(defn get-firsts [row]
  (map #(get % 1) row))

(with-open [out-file (io/writer "/Users/sergio/Dropbox/KEMLG/articulos/AAMAS 2012/WoW/data/wow-data.csv")]
  (csv/write-csv
    out-file
    (cons (map #(get % 0) (first all-data))
          (map get-firsts all-data))))

(with-open [out-file (io/writer "/Users/sergio/Dropbox/KEMLG/articulos/AAMAS 2012/WoW/data/wow-questions.csv")]
  (csv/write-csv
    out-file
    (cons (take 42 (map #(get % 0) (first all-data)))
          (map #(take 42 (get-firsts %)) all-data))))

(with-open [out-file (io/writer "/Users/sergio/Dropbox/KEMLG/articulos/AAMAS 2012/WoW/data/wow-achievements.csv")]
  (csv/write-csv
    out-file
    (cons (cons "id" (drop 42 (map #(get % 0) (first all-data))))
          (map #(cons (first (get-firsts %)) (drop 42 (get-firsts %))) all-data))))

