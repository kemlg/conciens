(ns conciens.core
  (:require [langohr.core          :as rmq]
            [langohr.channel       :as lch]
            [langohr.queue         :as lq]
            [langohr.consumers     :as lc]
            [langohr.basic         :as lb]
            [clojure.tools.logging :as log]
            [clojure.core.async    :as async
             :refer [go chan >! <!! close! sliding-buffer >!!]]
            [clj-bson.core         :as bson]))

(defn message-handler
  [core-ch ch
   {:keys [content-type delivery-tag type] :as meta}
   ^bytes payload]
  #_(log/info (format (str "[consumer] Received a message: %s, "
                          "delivery tag: %d, content type: %s, type: %s")
                     (bson/decode payload)
                     delivery-tag content-type type))
  (let [data (bson/decode payload)]
    (>!! core-ch data)))

(defn connect-amqp! [ip]
  (rmq/connect {:hosts [ip]}))

(defprotocol QueueConnection
  (publish! [this msg])
  (disconnect-queue! [this]))

(defrecord AmqpQueue [amqp-ch core-ch qname]
  QueueConnection
  (publish! [this msg]
    (lb/publish amqp-ch "amq.direct" qname
                (bson/encode msg)
                {:content-type "application/bson"}))
  (disconnect-queue! [this]
    (close! core-ch)
    (rmq/close amqp-ch)))

(defn connect-queue! [conn type qname]
  (let [ch (lch/open conn)
        core-ch (chan (sliding-buffer 16384))]
    #_(log/info "[main] Connected. Channel id:" (.getChannelNumber ch))
    (lq/declare ch qname {:exclusive (= type :reader)
                          :auto-delete true})
    (lq/bind ch qname "amq.direct" {:routing-key qname})
    (when (= type :reader)
      #_(lb/qos ch 20000)
      (lc/subscribe ch qname (partial message-handler core-ch)
                    {:auto-ack true}))
    (->AmqpQueue ch core-ch qname)))

(defn disconnect-amqp! [conn]
  (log/debug "[main] Disconnecting...")
  (rmq/close conn))

(defrecord WoWConnection [conn queue-events queue-actions])

(defn init-wow! []
  (let [conn (connect-amqp! "127.0.0.1")
        qc (connect-queue! conn :reader "conciens.events")
        qa (connect-queue! conn :writer "conciens.actions")]
    (->WoWConnection conn qc qa)))

(defn shutdown-wow! [all-conn]
  (disconnect-queue! (:queue-events all-conn))
  (disconnect-queue! (:queue-actions all-conn))
  (disconnect-amqp! (:conn all-conn)))

(defn benchmark []
  (let [all-conn (init-wow!)
        init (System/currentTimeMillis)
        total-events 50000]
    (loop [elem (<!! (:core-ch (:queue-events all-conn))) i 0]
      (if (>= i total-events)
        (println (double (/ total-events
                            (/ (- (System/currentTimeMillis) init)
                               1000))))
        (recur (<!! (:core-ch (:queue-events all-conn)))
               (inc i))))))
