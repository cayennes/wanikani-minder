(ns wanikani-minder.db.goal
  (:refer-clojure :exclude [get])
  (:require [clojure.set :refer [rename-keys]]
            [hugsql.core :as hugsql]
            [to-jdbc-uri.core]
            [wanikani-minder.config :refer [config]]))

(hugsql/def-db-fns "wanikani_minder/db/sql/goal.sql")

(def db {:connection-uri (to-jdbc-uri.core/to-jdbc-uri (:database-url config))})

(defn- app-facing-keys
  [user-from-db]
  (rename-keys user-from-db
               {:beeminder_slug :beeminder-slug
                :beeminder_id :beeminder-id}))

(defn create!
  [user beeminder-slug beeminder-id]
  (create!* db
            {:wanikani_minder_user (:id user)
             :beeminder_slug beeminder-slug
             :beeminder_id beeminder-id}))

(defn get-by-beeminder-slug
  [user beeminder-slug]
  (-> (get-by-beeminder-slug*
       db
       {:beeminder_slug beeminder-slug
        :wanikani_minder_user (:id user)})
      (app-facing-keys)))

(defn get-by-beeminder-id
  [user beeminder-id]
  (-> (get-by-beeminder-id*
       db
       {:beeminder_id beeminder-id
        :wanikani_minder_user (:id user)})
      (app-facing-keys)))

(defn get-by-user
  [user]
  (->> (get-by-user* db {:wanikani_minder_user (:id user)})
       (map app-facing-keys)))

(defn delete!
  [goal]
  (delete!* db goal))
