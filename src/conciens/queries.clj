(ns conciens.queries
  (:require [somnium.congomongo :as m]
            [langohr.core       :as rmq]
            [langohr.channel    :as lch]
            [langohr.queue      :as lq]
            [langohr.consumers  :as lc]
            [langohr.basic      :as lb]
            [clj-bson.core      :as bson]))

#_(def conn (m/make-connection "mongodb://130.211.62.241/conciens"))

#_(defn clean-past []
  (:timestamp
    (first 
      (m/with-mongo conn
        (m/fetch :events
                 :where {}
                 :sort {:millis 1})))))

(def c (ref {}))
(def t (ref #{}))
(def nums (ref {}))

(defn message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  #_(println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                     (bson/decode payload) delivery-tag content-type type))
  (let [data (bson/decode payload)]
    (dosync
      (alter c
             (fn [m]
               (merge-with + m {(:app data) 1})))
      (alter t
             (fn [m]
               (conj m (:timestamp data))))
      (if (= (:app data) "EVENT_TYPE_CREATURE_UPDATE")
        (alter nums
               (fn [m]
                 (merge-with + m {(:0 (:num-values data)) 1})))))))

(loop []
  (let [conn  (rmq/connect {:hosts ["127.0.0.1"]})
        ch    (lch/open conn)
        qname "conciens.events"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/declare ch qname {:exclusive true :auto-delete true})
    (lq/bind ch qname "amq.direct" {:routing-key "conciens.events"})
    (lb/qos ch 20000)
    #_(lc/subscribe ch qname message-handler {:auto-ack true})
    (lc/subscribe ch qname message-handler {:auto-ack true})
    (Thread/sleep 1000)
    (println "[main] Disconnecting...")
    (clojure.pprint/pprint @c)
    (clojure.pprint/pprint @nums)
    (rmq/close ch)
    (rmq/close conn)
    (count @t))

  (let [conn  (rmq/connect {:hosts ["127.0.0.1"]})
        ch    (lch/open conn)
        qname "conciens.actions"]
    (lq/declare ch qname {:exclusive false :auto-delete true})
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/bind ch qname "amq.direct" {:routing-key "conciens.actions"})
    (lb/publish ch "amq.direct" qname (bson/encode {:action-id "print-queue-size"})
                {:content-type "application/bson"})
    (println "[main] Disconnecting...")
    (rmq/close ch)
    (rmq/close conn)
    (println (apply max @t)))
  (recur))

