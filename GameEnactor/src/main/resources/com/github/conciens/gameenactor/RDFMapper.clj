(ns com.github.conciens.gameenactor.RDFMapper)

(import (org.openrdf.repository.manager RepositoryManager RemoteRepositoryManager)
        (org.openrdf.repository.config RepositoryConfig)
        (org.openrdf.repository RepositoryConnection Repository)
        (org.openrdf.query Query QueryLanguage)
        (org.openrdf.model Resource)
        (org.openrdf.model.impl URIImpl LiteralImpl))

(defn iteration-seq [iteration]
  (iterator-seq
   (reify java.util.Iterator
     (hasNext [this] (.hasNext iteration))
     (next [this] (.next iteration))
     (remove [this] (.remove iteration)))))

(defn query-test [guid]
  (let [repositoryManager (RemoteRepositoryManager. "http://192.168.1.120:8080/openrdf-sesame")]
    (.initialize repositoryManager)
    (let [query-str (str "PREFIX conciens:<http://github.com/kemlg/conciens#>
PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?x
WHERE { ?x conciens:characters-guid \"" guid "\" .
        }")
          repository (.getRepository repositoryManager "conciens")
          repositoryConnection (.getConnection repository)
          query (.prepareQuery repositoryConnection (QueryLanguage/SPARQL) query-str)
          result (iteration-seq (.evaluate query))
          statements (map #(iteration-seq (.getStatements repositoryConnection (.getValue (first %)) nil (LiteralImpl. "1") true (into-array Resource []))) result)]
      (println statements)
      (.close repositoryConnection)
      (.shutDown repository)
      (.shutDown repositoryManager))))

(defn get-player [guid]
  (let [repositoryManager (RemoteRepositoryManager. "http://192.168.1.120:8080/openrdf-sesame")]
    (.initialize repositoryManager)
    (let [repository (.getRepository repositoryManager "conciens")
          repositoryConnection (.getConnection repository)
          char-statement (first (iteration-seq (.getStatements repositoryConnection nil (URIImpl. "http://github.com/kemlg/conciens#characters-guid") (LiteralImpl. (str guid)) true (into-array Resource []))))
          statements (first (iteration-seq (.getStatements repositoryConnection (.getSubject char-statement) (URIImpl. "http://github.com/kemlg/conciens#characters-name") nil true (into-array Resource []))))
          name (.getObject statements)]
      (.close repositoryConnection)
      (.shutDown repository)
      (.shutDown repositoryManager)
      (.stringValue name))))

;(time (query-test 1))
;(time (get-player 1))


