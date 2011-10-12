(ns WowHead
  (:use Armory)
  (:use clojure.contrib.sql)
  (:use Utilities))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader ByteArrayInputStream))

(defn find-end [text]
  (+ (. text indexOf "}]});") 2))

(defn find-map [text]
  (+ 20 (. text indexOf "_truncated: 1, data:")))

(defn extract-json [text]
  (println (find-map text))
  (println (find-end text))
  
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

(defn mapa [txt]
  (get-map (extract-json
    (fetch-url txt))))

(defn store-in [x cl race]
  (println x cl race)
  (try
    (execute-player x cl race)
    (catch Exception e
      (println "Error when parsing" e))))

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
(def vclasses (vector 4 5 6 7 8 9 11 1 2 3)) ;-- Falta 1
(def vraces (vector 22 1 2 3 4 5 6 7 8 9 10 11 )) ;-- Falta 1

(defn get-wowhead [limit cl race]
;  (println (sql/connection))
(clojure.contrib.sql/with-connection
  db
  (println limit cl race)
  (let [url (str "http://www.wowhead.com/profiles=eu?filter=cl=" cl ";ra=" race ";minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:" limit)]
    (println "Reading " url)
    (try
      (println "get-wowhead: " (find-connection))
      (dorun (map #(store-in % cl race) (mapa url)))
      (catch Exception e (println e) (println (. e printStackTrace)))))))

;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:0"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:50"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:100"))
;(map store-in (mapa "http://www.wowhead.com/profiles=eu?filter=cl=2;ra=1;minle=85;maxle=85;cr=5:6:7;crs=1:1:1;crv=1:1:1;ma=1#characters:150"))

(def combs (shuffle (map flatten (cartesian-product (cartesian-product vlimits vclasses) vraces))))

(defn run-thread [a b c]
  (future (get-wowhead a b c)))

(defn execute-multithread [x]
  (dorun (map deref (dorun
    (map #(apply run-thread %) x)))))

(defn update-db-wowhead []
  (dorun (map execute-multithread (partition-all 5 combs))))
;(println "!!!!!!!!!!!!!!!!!!!! FINISHED !!!!!!!!!!!!!!!!!!!!")

;(descarga "http://eu.battle.net/wow/en/character/wildhammer/apphia/achievement/92")

;(get-wowhead 50 5 9)

