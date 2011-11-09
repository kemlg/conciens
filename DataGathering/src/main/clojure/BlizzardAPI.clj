(ns BlizzardAPI
  (:require [clj-http.client :as client])
  (:require [GDocs :as gdocs])
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as st])
  (:require [ParseExcelDocs :as excel])
  (:require [somnium.congomongo :as cm]))

; ((client/get "http://eu.battle.net/api/wow/character/Dun%20Modr/Nobundia?fields=guild,stats,talents,items,reputation,titles,professions,appearance,companions,mounts,pets,achievements,progression,pvp,quests" {:accept :json}) :body)
(def conn (load-file "./contrib/mongodb.clj"))

(defn get-records []
  (:content (first (:content (first (excel/google-docs-treatment))))))

(defn is-question? [x]
  (or
    (. (first x) startsWith "How")
    (. (first x) startsWith "Would")
    (= (. (first x) charAt 1) \))
    (= (. (first x) charAt 2) \))))

(defn assign-keys [record keyset]
  (def as-map (apply hash-map (interleave keyset record)))
  (def questions {:questions (into {} (filter is-question? as-map))})
  (def basics (filter (complement is-question?) as-map))
  (merge (into {} basics) questions))

(defn normalize-json [k]
  (. k replaceAll "\\." "-"))

(defn clean-record-map [record-map]
  (map normalize-json (filter #(not (= % "Unused")) (map :value record-map))))

(defn create-associative-map [plain-map-with-header]
  (def keyset (clean-record-map (first plain-map-with-header)))
  (def valueset (rest plain-map-with-header))
  (map #(assign-keys (map :value %) keyset) valueset))

(defn add-info [record]
  (try
    (def url (get record "Main Character Profile URL"))
    (def ss (st/split url #"\/"))
    (def res (apply str (interpose "/" (concat (list "http:/" (nth ss 2) (nth ss 3) "en") (drop 5 ss)))))
    (assoc (assoc (assoc record :realm (nth ss 6)) :char (nth ss 7)) :host (nth ss 2))
    (catch Exception e
      nil)))

(defn get-clean-players []
  (filter #(not (nil? %)) (map add-info (create-associative-map (get-records)))))

(defn call-url [url]
  (client/get url {:accept :json :throw-exceptions false}))

(defn process-record [record]
  (def url (str "http://" (:host record) "/api/wow/character/" (:realm record) "/" (:char record) "?fields=guild,stats,talents,items,reputation,titles,professions,appearance,companions,mounts,pets,achievements,progression,pvp,quests"))
  (loop [result (call-url url)]
    (if (not (= (:body result) ""))
      (if (= (:body result) "{\"status\":\"nok\", \"reason\": \"Character not found.\"}")
        nil
        (merge record (json/read-json (:body result))))
      (recur (call-url url)))))

(defn valid-result? [result]
  (not (or (nil? result))))

(defn store-mongo [js]
  ;(json/json-str js))
  (cm/with-mongo conn (cm/insert! :players js)))

(def all-records (get-clean-players))

(dorun (map store-mongo (filter valid-result? (map process-record all-records))))



