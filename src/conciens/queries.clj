(ns conciens.queries
  (:require [somnium.congomongo :as m]
            [langohr.core       :as rmq]
            [langohr.channel    :as lch]
            [langohr.queue      :as lq]
            [langohr.consumers  :as lc]
            [langohr.basic      :as lb]
            [clj-bson.core      :as bson]))

(def conn (m/make-connection "mongodb://130.211.62.241/conciens"))

(defn clean-past []
  (:timestamp
    (first 
      (m/with-mongo conn
        (m/fetch :events
                 :where {}
                 :sort {:millis 1})))))

(def c (ref 0))

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  #_(println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                    (bson/decode payload) delivery-tag content-type type))
  (dosync (alter c inc)))


(let [conn  (rmq/connect {:hosts ["130.211.62.241"]})
      ch    (lch/open conn)
      qname "conciens.events"]
  (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
  (lq/declare ch qname {:exclusive false :auto-delete true})
  (lq/bind ch qname "amq.direct" {:routing-key "conciens.events"})
  #_(lc/subscribe ch qname message-handler {:auto-ack true})
  (lc/subscribe ch qname message-handler {:auto-ack true})
  (Thread/sleep 2000)
  (println "[main] Disconnecting...")
  (println @c)
  (rmq/close ch)
  (rmq/close conn))

#_(let [conn  (rmq/connect {:hosts ["130.211.62.241"]})
       ch    (lch/open conn)
       qname "conciens.actions"]
   (lq/declare ch qname {:exclusive false :auto-delete true})
   (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
   (lq/bind ch qname "amq.direct" {:routing-key "conciens.actions"})
   (lb/publish ch "amq.direct" qname (bson/encode {:action-id "add-quest"})
               {:content-type "application/bson"})
   (println "[main] Disconnecting...")
   (rmq/close ch)
   (rmq/close conn))
