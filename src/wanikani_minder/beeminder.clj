(ns wanikani-minder.beeminder
  (:require [clj-http.client :as client]
            [wanikani-minder.config :refer [config]]))

(def api-url "https://www.beeminder.com/api/v1")

(defn update-goal
  [{:keys [beeminder-access-token]} goal data]
  (client/put (str api-url "/users/me/goals/" goal ".json")
              {:form-params (merge data
                                   {"access_token" beeminder-access-token})
               :redirect-strategy :lax}))

(defn register-autofetch
  [user goal]
  (let [source-name (get-in config [:beeminder :client-name])]
    ;; TODO: also make it an odometer and use max
    (update-goal user goal {"datasource" source-name})))

(defn add-datapoint
  [{:keys [beeminder-access-token]} goal {:keys [value comment]}]
  (client/post (str api-url "/users/me/goals/" goal "/datapoints.json")
               {:form-params {"value" value
                              "comment" comment ;; TODO: make this optional
                              "access_token" beeminder-access-token}}))

;; these have only been used in the repl so far

(defn user-info
  [{:keys [beeminder-access-token]}]
  (:body (client/get (str api-url "/users/me.json")
                     {:query-params {"access_token" beeminder-access-token}
                      :as :json})))

(defn goal-list
  [{:keys [beeminder-access-token]}]
  (:body (client/get (str api-url "/users/me/goals.json")
                     {:query-params {"access_token" beeminder-access-token}
                      :as :json})))

(defn goal-info
  [{:keys [beeminder-access-token] :as user} goal]
  (:body (client/get (str api-url "/users/me/goals/" goal ".json")
                     {:query-params {"access_token" beeminder-access-token}
                      :as :json})))
