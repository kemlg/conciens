(ns conciens.handler
  "Specification of the API frontend for a Ring-based server."
  (:use compojure.core)
  (:use [clojure.java.shell :only [sh]])
  (:require [ring.util.response :as resp]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.response :as response]
            [noir.session :as session]
            [clojure.data.json :as json]
            [noir.util.middleware :as middleware]
            [somnium.congomongo :as m]
            [clojure.core.async :as async :refer [chan go <! >! go-loop]]))

(def ch (chan 50000))

(defn with-gzip
  "Ring middleware that takes an HTTP response and compresses its body in GZIP
  format."
  [handler] 
  (fn [request] 
    (let [response (handler request) 
          out (java.io.ByteArrayOutputStream.) 
          accept-encoding (.get (:headers request) "accept-encoding")] 
      (if (and (not (nil? accept-encoding)) 
               (re-find #"gzip" accept-encoding)) 
        (do 
          (doto (java.io.BufferedOutputStream. 
                  (java.util.zip.GZIPOutputStream. out)) 
            (.write (.getBytes (:body response))) 
            (.close))
          {:status (:status response) 
           :headers (assoc (:headers response) 
                           "Content-Type" "application/json" 
                           "Content-Encoding" "gzip") 
           :body (java.io.ByteArrayInputStream. (.toByteArray out))}) 
        response))))

(defmulti process-item (fn [item] (nil? (:player item))))

(defmethod process-item false
  [item]
  (println item))

(defmethod process-item :default
  [_])

(def actions (ref []))
(def commands (ref []))

(def conn (m/make-connection :conciens))
(m/set-connection! conn)
(m/set-write-concern conn :errors-ignored)

(go-loop [item (<! ch)]
  (m/insert! :events item)
  (if (not (nil? (:player item)))
    (do
      (println item)
      (if (not (nil? (:x (:player item))))
        (dosync
          (alter actions (fn [old-actions]
                           old-actions
                           (concat
                             @commands
                             old-actions)))
          (alter commands (fn [_] []))))))
  (recur (<! ch)))

(defn command [& args]
  (dosync
    (alter commands
           (fn [old-commands]
             (conj old-commands
                   (apply hash-map args))))))

(defn create-response []
  (dosync
    (let [acts @actions]
      (alter actions (fn [old-actions] []))
      (clojure.pprint/pprint {:test "ok" :actions acts})
      (response/json {:test "ok" :actions acts}))))

(defroutes app-routes
  (GET "/actions" []
       (create-response))
  (POST "/event" {body :body}
        (let [inputs (json/read-str (slurp body) :key-fn keyword)]
          (dorun (map (fn [i] (go (>! ch i))) inputs)))
        (create-response))
  
  ;; Other
  (POST "/webhook" [] (do
                        (println "pulling from repo...")
                        (sh "/usr/bin/git" "pull")))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  "The object specified in `project.clj` as the Ring handler of the `atalaya`
  server."
  (middleware/app-handler [(middleware/app-handler
                             [app-routes]
                             :middleware [with-gzip])]))
