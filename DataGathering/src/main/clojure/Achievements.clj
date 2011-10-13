(ns Achievements
  (:require [clojure.java.jdbc :as sql])
  (:use Utilities))

(defn insert-achievement [row]
  (sql/with-connection
    db
    (sql/insert-values
    :achievements
    [:name]
    [(:achievement row)])))

(defn update-db-achievements []
  (sql/with-connection
    db
    (sql/with-query-results rs ["SELECT DISTINCT achievement FROM achievements_players WHERE achievement NOT IN (SELECT name FROM achievements);"]
      (doall (map insert-achievement rs)))))

