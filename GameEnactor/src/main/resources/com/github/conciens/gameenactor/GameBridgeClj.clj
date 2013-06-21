(ns com.github.conciens.gameenactor.GameBridgeClj
  (require [clj-time.local :as cljt]
           [clj-time.coerce :as cljtc]))

(import (java.net ServerSocket)
        (java.io BufferedReader InputStreamReader)
        (net.sf.ictalive.operetta.OM Atom Constant OMFactory)
        (net.sf.ictalive.runtime.action ActionFactory MatchmakerQuery)
        (net.sf.ictalive.runtime.event Actor Cause Event EventFactory Key ObserverView)
        (net.sf.ictalive.runtime.fact Content FactFactory Message SendAct)
        (eu.superhub.wp4.monitor.eventbus EventBus)
        (eu.superhub.wp4.monitor.eventbus.exception EventBusConnectionException))

(def msg-count (ref 0))
(def total-msgs (ref 0))
(def init-time (ref (System/currentTimeMillis)))

(defn process-line [txt ebjt]
  (let [splitted-text (re-seq #"[^\|]+" txt)
      ev (.createEvent EventFactory/eINSTANCE)
      c (.createContent FactFactory/eINSTANCE)
      sa (.createSendAct FactFactory/eINSTANCE)
      ms (.createMessage FactFactory/eINSTANCE)
      a (.createAtom OMFactory/eINSTANCE)
      myActor (.createActor EventFactory/eINSTANCE)
      myKey (.createKey EventFactory/eINSTANCE)
      myView (.createObserverView EventFactory/eINSTANCE)
      myCause (.createCause EventFactory/eINSTANCE)
      provEv (.createEvent EventFactory/eINSTANCE)
      provKey (.createKey EventFactory/eINSTANCE)]
    (.setName myActor "WoWGameEnactor")
    (.setUrl myActor "alive.lsi.upc.edu")
    (.setId myKey (str (cljtc/to-long (cljt/local-now))))
    (.setLocalKey ev myKey)
    (.setAsserter ev myActor)
    (.setPointOfView ev myView)
    (.setFact c sa)
    (.setContent ev c)
    (.add (.getObject ms) a)
    (.setPredicate a (first splitted-text))
    (.addAll
      (.getArguments a)
      (doall
        (map #(let [ct (.createConstant OMFactory/eINSTANCE)]
                (.setName ct %)
                ct)
             (rest splitted-text))))
    (let [formula (str
                    (first splitted-text)
                    "("
                    (apply str (interpose ", " (rest splitted-text)))
                    ")")]
      (.setId provKey formula))
    (.setLocalKey provEv provKey)
    (.setEvent myCause provEv)
    (.add (.getProvenance ev) myCause)
    (.setSendMessage sa ms)
    (.publish ebjt ev)
    (dosync (alter msg-count dec))))

(defn process-socket [sock ebjt]
  (println sock)
  (let [buf (BufferedReader. (InputStreamReader. (. sock getInputStream)))]
    (loop [txt (. buf readLine)] 
      (if (nil? txt)
        (println "Closed!")
        (do
          (future (process-line txt ebjt))
          (dosync (alter msg-count inc) (alter total-msgs inc))
	        (recur (. buf readLine))))))
  (. sock close))

(defn give-stats [ebjt]
  (loop [b false]
    (if b
      nil
      (do
        (Thread/sleep 5000)
        (let [pending-messages @msg-count
              msgs-per-s (double (/ @total-msgs (/ (- (System/currentTimeMillis) @init-time) 1000)))
              queue-status (.waitingForDispatch ebjt)]
          (println "Pending messages: " pending-messages " | Messages per second: " msgs-per-s " | Serializing queue size: " queue-status))
        (recur false)))))

;; main
(defn main []
  (let [ebjt (EventBus. "192.168.1.120" "7676" false)]
    (future (give-stats ebjt))
    (loop [ssin (ServerSocket. 6969) ssout (ServerSocket. 6970)]
    (if (. ssin isClosed)
      nil
      (do
        (let [sock (. ssin accept)]
          (future (process-socket sock ebjt)))
        (recur ssin ssout))))))

(main)
