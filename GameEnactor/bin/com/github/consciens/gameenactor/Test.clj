(ns com.github.consciens.gameenactor.Test)
(import 
  '(com.github.consciens.gameenactor EventBusJavaTest)  
  '(java.util Random)
 )


(def rnd (new Random))
(. rnd nextInt 500)

(def ebjt (new EventBusJavaTest))
(. ebjt SubmitTestEvent "This is Sparta!")

;; main
(defn main []
  (println "MAIN!!")
  )

(re-seq #"[^\|]+" "UPDATE|A TOMAR|POR CULO")
(nth (re-seq #"[^\|]+" "UPDATE|A TOMAR|POR CULO") 0)

(def splitted_text (re-seq #"[^\|]+" "UPDATE|A TOMAR|POR CULO"))

(cond 
  (= (nth splitted_text 2 "not-found") "POR ACULO") (println "POR CULO")
  (= (nth splitted_text 0 "not-found") "AUPDATE") (println "UPDATE")
  (= (nth splitted_text 1 "not-found") "A TOMAR") (println "A TOMAR")
  :else (println "nada de nada")
)

(re-seq #"\w+" "the quick brown fox")


