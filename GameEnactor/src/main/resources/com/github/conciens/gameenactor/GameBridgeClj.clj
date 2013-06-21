(ns com.github.conciens.gameenactor.GameBridgeClj
  (require [clj-time.core :as cljt]
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

;  <event:Event asserter=\"/5\" timestamp=\"2013-06-21T13:35:06.425+0200\">
(defn generate-xml [actor-name actor-url splitted-text]
  (let [predicate (first splitted-text)
        formula (str (first splitted-text) "(" (apply str (interpose ", " (rest splitted-text))) ")")
        number-of-params (count (rest splitted-text))
        arguments (apply str (interpose " " (map #(str "/" %) (range 2 (+ 2 number-of-params)))))]
    (str
"<?xml version=\"1.0\" encoding=\"ASCII\"?>
<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:event=\"http://ict-alive.sourceforge.net/RunTime/events\" xmlns:fact=\"http://ict-alive.sourceforge.net/RunTime/facts\" xmlns:net.sf.ictalive.operetta=\"http://ict-alive.sourceforge.net/operetta/OM/1.0\">
  <event:Event asserter=\"/" (+ 3 number-of-params) "\" timestamp=\"" (cljt/now) "\">
    <localKey id=\"" (str (java.util.UUID/randomUUID)) "\"/>
    <content>
      <fact xsi:type=\"fact:SendAct\">
        <sendMessage object=\"/1\"/>
      </fact>
    </content>
    <pointOfView xsi:type=\"event:ObserverView\"/>
    <provenance event=\"/" (+ 2 number-of-params) "\"/>
  </event:Event>
  <net.sf.ictalive.operetta:Atom predicate=\"" predicate "\" arguments=\"" arguments "\"/>\n"
  (apply str (interpose "\n" (map #(str "  <net.sf.ictalive.operetta:Constant name=\"" % "\"/>") (rest splitted-text))))
  "\n  <event:Event>
    <localKey id=\"" formula "\"/>
  </event:Event>
  <event:Actor url=\"" actor-url "\" emit=\"/0\" name=\"" actor-name "\"/>
</xmi:XMI>")))

(defn process-line [txt ebjt]
    (let [splitted-text (re-seq #"[^\|]+" txt)
          actor-name "WoWGameEnactor"
          actor-url "alive.lsi.upc.edu"]
    (.publish ebjt (generate-xml actor-name actor-url splitted-text))
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
              queue-status (.waitingForDispatch ebjt)
              available (.available ebjt)]
          (println "Pending messages: " pending-messages " | Messages per second: " msgs-per-s " | Serializing queue size: " queue-status " | Deserializing queue size: " available))
        (recur false)))))

;; main
(defn main []
  (let [ebjt (EventBus. "192.168.1.120" "7676" false)]
    (.activateSubscription ebjt false)
    (future (give-stats ebjt))
    (loop [ssin (ServerSocket. 6969) ssout (ServerSocket. 6970)]
    (if (. ssin isClosed)
      nil
      (do
        (let [sock (. ssin accept)]
          (future (process-socket sock ebjt)))
        (recur ssin ssout))))))

(main)
