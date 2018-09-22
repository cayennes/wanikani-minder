(ns wanikani-minder.db.user
  (:refer-clojure :exclude [get])
  (:require [clojure.set :refer [rename-keys]]
            [hugsql.core :as hugsql]
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

(defn update-wanikani-token! [beeminder-username wanikani-api-key]
  (update-wanikani-api-key!*
   db
   {:beeminder_id beeminder-username
    :wanikani_api_key wanikani-api-key}))

(defn update-beeminder-goal-slug! [beeminder-username beeminder-goal-slug]
  (update-beeminder-goal-slug!*
   db
   {:beeminder_id beeminder-username
    :beeminder_goal_slug beeminder-goal-slug}))

(defn get
  [beeminder-username]
  (-> (get* db {:beeminder_id beeminder-username})
      (rename-keys {:beeminder_id :beeminder-id
                    :beeminder_access_token :beeminder-access-token
                    :beeminder_goal_slug :beeminder-goal-slug
                    :wanikani_api_key :wanikani-api-key})))
