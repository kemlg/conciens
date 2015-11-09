(defproject game-enactor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repositories [["alive-maven" "http://tranchis.github.com/alive-maven/"]
                 ["kermeta" "http://maven.irisa.fr/artifactory/list/kermeta-public-release/"]
                 ["acceleo-releases" "https://repo.eclipse.org/content/repositories/acceleo-releases/"]
                 ["bintray" "http://dl.bintray.com/tranchis/wire"]]
  :dependencies  [[org.clojure/clojure "1.7.0"]
                  [org.clojure/core.async "0.2.371"]
                  [compojure "1.1.5"]
                  [clj-http "0.7.7"]
                  [lib-noir "0.7.9"]
                  [congomongo "0.4.1"]
                  [clj-http "0.9.1"]
                  [org.clojure/tools.logging "0.3.1"]
                  [com.github.tranchis/clj-bson "0.2.0"]
                  [com.novemberain/langohr "3.0.0-rc2"]
                  [org.clojure/core.async "0.1.303.0-886421-alpha"]
                  [org.clojure/data.json "0.2.4"]
                  [net.sf.ictalive/services "1.0.3"]
                  [net.sf.ictalive/XSDSchema "1.0.0"]
                  [net.sf.ictalive/operetta "1.0.0"]
                  [net.sf.ictalive/runtime "1.0.0"]
                  [net.sf.ictalive/coordination "1.0.2"]
                  [net.sf.ictalive/owls "1.0.1"]
                  [net.sf.ictalive/rules "1.0.4"]
                  [net.sf.ictalive/normInstances "1.0.0"]
                  [org.eclipse.emf/org.eclipse.emf.ecore "2.9.1.v20130827-0309"]
                  [org.eclipse.emf/org.eclipse.emf.common "2.9.1.v20130827-0309"]
                  #_[com.github.tranchis/wire "0.1.0"]
                  [clj-time "0.5.1"]]
  :plugins [[lein-ring "0.8.5"]
            [no-man-is-an-island/lein-eclipse "2.0.0"]]
  :java-source-paths ["java"]
  :nrepl {:start? true :port 3001}
  :main conciens.game-bridge
  :ring {:handler conciens.handler/app}
  :profiles
  {:dev  {:dependencies  [[javax.servlet/servlet-api "2.5"]
                          [ring-mock "0.1.5"]]}})

