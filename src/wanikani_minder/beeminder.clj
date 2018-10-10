(ns wanikani-minder.beeminder
  (:require [clj-http.client :as client]
            [wanikani-minder.config :refer [config]]))

(def api-url "https://www.beeminder.com/api/v1")

;; TODO: consider some kind of automatic update for this
(def total-wanikani-items (+ 447 ;; "radicals"
                             2027 ;; kanji
                             6300)) ;; vocabulary items

(defn add-datapoint
  [{:keys [beeminder-access-token]} goal {:keys [value comment]}]
  (client/post (str api-url "/users/me/goals/" goal "/datapoints.json")
               {:form-params {"value" value
                              "comment" comment ;; TODO: make this optional
                              "access_token" beeminder-access-token}}))

(defn create-goal
  [{:keys [beeminder-access-token]} options]
  (client/post (str api-url "/users/me/goals.json")
               {:form-params (merge options
                                    {"access_token" beeminder-access-token})
                :as :json
                :coerce :always
                :throw-exceptions false}))

(defn update-goal
  [{:keys [beeminder-access-token]} goal data]
  (client/put (str api-url "/users/me/goals/" goal ".json")
              {:form-params (merge data
                                   {"access_token" beeminder-access-token})
               :redirect-strategy :lax
               :as :json
               :coerce :always
               :throw-exceptions false}))

(defn create-wanikani-minder-goal
  [user
   {:keys [slug rate start-value]}]
  (if-let [errors (not-empty (merge (if-not (not-empty slug) {:slug "Required"})
                                    (if-not (not-empty rate) {:rate "Required"})
                                    (if-not start-value {:unexpected "Could not determine start value"})))]
    errors
    (let [create-result (->> {:slug slug
                              :rate rate
                              :goalval total-wanikani-items
                              :initval start-value
                              :datasource (get-in config [:beeminder :client-name])
                              :goal_type "custom"}
                             (create-goal user)
                             :body)]
      (if-let [errors (:errors create-result)]
        (merge (select-keys errors [:slug])
               (if-let [rate-error (:rfin errors)]
                 {:rate rate-error})
               (if-let [unexpected (not-empty (dissoc errors :slug :rfin))]
                 {:unexpected unexpected}))
        (let [update-result (->> {:aggday "max"
                                  :kyoom false
                                  :integery true}
                                 (update-goal user slug))]
          (if-let [errors (:errors update-result)]
            {:unexpected errors}))))))

;; things below here are currently tools for the repl rather than used by the app
;; get a user in repl with (wanikani-minder.db.user/get "beeminder-username")

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

(defn update-goal
  [{:keys [beeminder-access-token]} goal data]
  (client/put (str api-url "/users/me/goals/" goal ".json")
              {:form-params (merge data
                                   {"access_token" beeminder-access-token})
               :redirect-strategy :lax}))

(defn register-autofetch
  [user goal]
  (let [source-name (get-in config [:beeminder :client-name])]
    (update-goal user goal {"datasource" source-name})))
