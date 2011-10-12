(ns Achievements
  (:use [clojure.contrib.sql])
  (:use Utilities))

(defn insert-achievement [row]
  (with-connection
    db
    (insert-values
    :achievements
    [:name]
    [(:achievement row)])))

(defn update-db-achievements []
  (with-connection
    db
    (with-query-results rs ["SELECT DISTINCT achievement FROM achievements_players;"]
      (doall (map insert-achievement rs)))))

