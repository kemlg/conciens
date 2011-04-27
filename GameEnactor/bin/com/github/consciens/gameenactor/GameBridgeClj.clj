(ns com.github.consciens.gameenactor.GameBridgeClj)
(import 
 '(java.net ServerSocket)
 '(java.io BufferedReader InputStreamReader)
 '(com.github.consciens.gameenactor EventBusJavaTest)  
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

(def ebjt (new EventBusJavaTest))

(defn process-line [txt]
    (if (. txt contains "UPDATE") 
      ((. ebjt SubmitTestEvent txt)) 
      (println (str "This is not an update: [" txt "]"))
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
