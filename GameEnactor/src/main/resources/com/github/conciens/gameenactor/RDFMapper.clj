(ns com.github.conciens.gameenactor.RDFMapper)

(import (org.openrdf.repository.manager RepositoryManager RemoteRepositoryManager)
        (org.openrdf.repository.config RepositoryConfig)
        (org.openrdf.repository RepositoryConnection Repository)
        (org.openrdf.query Query QueryLanguage)
        (org.openrdf.model Resource)
        (org.openrdf.model.impl URIImpl))

(defn iteration-seq [iteration]
  (iterator-seq
   (reify java.util.Iterator
     (hasNext [this] (.hasNext iteration))
     (next [this] (.next iteration))
     (remove [this] (.remove iteration)))))

(defn query-test []
  (let [repositoryManager (RemoteRepositoryManager. "http://192.168.1.120:8080/openrdf-sesame")]
    (.initialize repositoryManager)
    (let [query-str "PREFIX conciens:<http://github.com/kemlg/conciens#>
PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?x
WHERE { ?x conciens:characters-name \"Lolaila\" .
        }"
          repository (.getRepository repositoryManager "conciens")
          repositoryConnection (.getConnection repository)
          query (.prepareQuery repositoryConnection (QueryLanguage/SPARQL) query-str)
          result (iteration-seq (.evaluate query))
          statements (map #(iteration-seq (.getStatements repositoryConnection (.getValue (first %)) (URIImpl. "http://github.com/kemlg/conciens#gender") nil true (into-array Resource []))) result)]
      (println statements)
      (.close repositoryConnection)
      (.shutDown repository)
      (.shutDown repositoryManager))))

(time (query-test))