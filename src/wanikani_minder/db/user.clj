(ns wanikani-minder.db.user
  (:require [hugsql.core :as hugsql]
            [to-jdbc-uri.core]
            [wanikani-minder.config :refer [config]]))

(hugsql/def-db-fns "wanikani_minder/db/sql/user.sql")

(def db {:connection-uri (to-jdbc-uri.core/to-jdbc-uri (:database-url config))})

(defn beeminder-create-or-update! [{:keys [username access-token]}]
  "store user"
  (beeminder-create-or-update!*
   db
   {:beeminder_id username
    :beeminder_access_token access-token}))
