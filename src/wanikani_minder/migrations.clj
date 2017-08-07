(ns wanikani-minder.migrations
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [wanikani-minder.config :refer [config]]))

(defn load-config
  []
  {:datastore (jdbc/sql-database (:database-url config))
   :migrations (jdbc/load-resources "migrations")})

(defn migrate
  []
  (repl/migrate (load-config)))

(defn rollback
  []
  (repl/rollback (load-config)))
