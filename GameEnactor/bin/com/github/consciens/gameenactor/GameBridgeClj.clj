(ns com.github.consciens.gameenactor.GameBridgeClj)
(import 
 '(java.net ServerSocket)
 '(java.io BufferedReader InputStreamReader)
 '(com.github.consciens.gameenactor EventBusJavaBridge)  
)
;Java Code
;		ServerSocket	ssin, ssout;
;		Socket			sin ,sout;
;		InputStream		is;
;		BufferedReader	br;
;		String			line;
;		
;		ssin = new ServerSocket(Constants.SOCK_PORT_IN);
;		ssout = new ServerSocket(Constants.SOCK_PORT_OUT);
;		while(!ssin.isClosed())
;		{
;			sin = ssin.accept();
;			System.out.println("Connection in!");
;			sout = ssout.accept();
;			System.out.println("Connection out!");
;			
;			is = sin.getInputStream();
;			br = new BufferedReader(new InputStreamReader(is));
;			while(!sin.isClosed())
;			{
;				line = br.readLine();
;				System.out.println("Message: [" + line + "]");
;				line = line + "\n";
;				sout.getOutputStream().write(line.getBytes());
;			}
;		}

(def ebjt (new EventBusJavaBridge))

(defn process-line [txt]
    (def splitted_text (re-seq #"[^\|]+" txt))

		(cond 
		  (= (nth splitted_text 0 "not-found") "PVP_KILL") (. ebjt SendMessageEvent (str "PVP_KILL" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))      
      (= (nth splitted_text 0 "not-found") "CREATURE_KILL") (. ebjt SendMessageEvent (str "CREATURE_KILL" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "KILLED_BY_CREATURE") (. ebjt SendMessageEvent (str "KILLED_BY_CREATURE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "LEVEL_CHANGED") (. ebjt SendMessageEvent (str "LEVEL_CHANGED" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "MONEY_CHANGED") (. ebjt SendMessageEvent (str "MONEY_CHANGED" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "AREA_TRIGGER") (. ebjt SendMessageEvent (str "AREA_TRIGGER" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "WEATHER_CHANGE") (. ebjt SendMessageEvent (str "WEATHER_CHANGE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "WEATHER_UPDATE") (. ebjt SendMessageEvent (str "WEATHER_UPDATE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "EMOTE") (. ebjt SendMessageEvent (str "EMOTE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "HELLO") (. ebjt SendMessageEvent (str "HELLO" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "OBJECT_UPDATE") (. ebjt SendMessageEvent (str "OBJECT_UPDATE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "CREATURE_UPDATE") (. ebjt SendMessageEvent (str "CREATURE_UPDATE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_ACCEPT_ITEM") (. ebjt SendMessageEvent (str "QUEST_ACCEPT_ITEM" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_ACCEPT_OBJECT") (. ebjt SendMessageEvent (str "QUEST_ACCEPT_OBJECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "ITEM_USE") (. ebjt SendMessageEvent (str "ITEM_USE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "ITEM_EXPIRE") (. ebjt SendMessageEvent (str "ITEM_EXPIRE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "GOSSIP_SELECT") (. ebjt SendMessageEvent (str "GOSSIP_SELECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found")))
      (= (nth splitted_text 0 "not-found") "GOSSIP_SELECT_OBJECT") (. ebjt SendMessageEvent (str "GOSSIP_SELECT_OBJECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found")))
      (= (nth splitted_text 0 "not-found") "GOSSIP_SELECT_CODE") (. ebjt SendMessageEvent (str "GOSSIP_SELECT_CODE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found") " : " (nth splitted_text 5 "not-found")))
      (= (nth splitted_text 0 "not-found") "GOSSIP_SELECT_CODE_OBJECT") (. ebjt SendMessageEvent (str "GOSSIP_SELECT_CODE_OBJECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found") " : " (nth splitted_text 5 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_SELECT") (. ebjt SendMessageEvent (str "QUEST_SELECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_COMPLETE") (. ebjt SendMessageEvent (str "QUEST_COMPLETE" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_REWARD") (. ebjt SendMessageEvent (str "QUEST_REWARD" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found")))
      (= (nth splitted_text 0 "not-found") "QUEST_REWARD_OBJECT") (. ebjt SendMessageEvent (str "QUEST_REWARD_OBJECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found") " : " (nth splitted_text 4 "not-found")))
      (= (nth splitted_text 0 "not-found") "GET_DIALOG_STATUS") (. ebjt SendMessageEvent (str "GET_DIALOG_STATUS" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))
      (= (nth splitted_text 0 "not-found") "GET_DIALOG_STATUS_OBJECT") (. ebjt SendMessageEvent (str "GET_DIALOG_STATUS_OBJECT" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found")))      
      (= (nth splitted_text 0 "not-found") "OBJECT_DESTROYED") (. ebjt SendMessageEvent (str "OBJECT_DESTROYED" " : " (nth splitted_text 1 "not-found") " : " (nth splitted_text 2 "not-found") " : " (nth splitted_text 3 "not-found")))
		  :else (println (str "This is not an event: [" txt "]"))
		)    
)

(defn process-socket [sock]
  (println sock)
  (let [buf (BufferedReader. (InputStreamReader. (. sock getInputStream)))]
	  (loop [txt (. buf readLine)] 
	    (if (nil? txt)
	      (println "Closed!")
	      (do
	        (future (process-line txt))
	        (recur (. buf readLine))
	      )
	    )
	  )
  )
  (. sock close)
)

;; main
(defn main []
  (loop [ssin (ServerSocket. 6969) ssout (ServerSocket. 6970)]
    (if (. ssin isClosed)
      nil
      (do
        (let [sock (. ssin accept)]
          (future (process-socket sock)))
        (recur ssin ssout)
      )
    )
  )
)
