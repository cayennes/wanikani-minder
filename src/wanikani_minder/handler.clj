(ns wanikani-minder.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.logger :as logger]
            [ring.util.codec :refer [url-encode]]
            [ring.util.response :as response]
            [ring.middleware.json :as json]
            [wanikani-minder.beeminder :as beeminder]
            [wanikani-minder.config :refer [config]]
            [wanikani-minder.pages :as pages]
            [wanikani-minder.db.goal :as goal]
            [wanikani-minder.db.user :as user]
            [wanikani-minder.wanikani :as wanikani]))

;; # core

;; this is the entirety of core logic for this app
;; not really worth a namespace

(defn maintained-progress
  [wanikani-key]
  (- (wanikani/get-total wanikani-key) (wanikani/get-due-count wanikani-key)))

;; auth

(defn redirect-uri
  []
  (str (:base-url config) "auth/beeminder/callback"))

(defn authorize-url
  []
  (str "https://www.beeminder.com/apps/authorize?"
       "client_id=" (get-in config [:beeminder :client-id])
       "&redirect_uri=" (url-encode (redirect-uri))
       "&response_type=token"))

(defn create-goal
  [beeminder-username {:keys [slug rate]}]
  (let [user (user/get beeminder-username)
        current-progress (-> user :wanikani-api-key maintained-progress)]
    (let [res (beeminder/create-wanikani-minder-goal
               user
               {:slug slug
                :rate rate
                :start-value current-progress})]
      (if (:errors res)
        res
        (do (goal/create! user slug (:id res))
            nil)))))

(defn homepage
  [{{beeminder-username :username} :beeminder :as session}
   {:keys [action] :as params}]
  (if beeminder-username
    (let [action-data (case action
                        "wanikani" (let [wanikani-api-key (:wanikani-api-key params)
                                         key-check (wanikani/get-username-or-error wanikani-api-key)]
                                     (if (:success key-check)
                                       (do
                                         (user/update-wanikani-token! beeminder-username
                                                                      wanikani-api-key)
                                         {:wanikani {:username (:username key-check)}})
                                       {:wanikani {:error (:error key-check)}}))
                        "create-goal" (if-let [errors (create-goal beeminder-username params)]
                                        {:create-goal {:errors errors
                                                       :success false}}
                                        {:create-goal {:success true
                                                       :slug (:slug params)}})
                        nil)
          user (user/get beeminder-username)
          user-data {:goals (map :beeminder-slug (goal/get-by-user user))
                     :wanikani-api-version (wanikani/api-key-version
                                            (:wanikani-api-key user))}]
      (pages/logged-in-homepage user user-data action-data))
    (pages/logged-out-homepage (authorize-url))))

(defn login
  [session access-token username]
  (user/beeminder-create-or-update! {:username username :access-token access-token})
  (assoc-in (response/redirect "/")
            [:session :beeminder] {:username username :token access-token}))

(defn logout
  [session]
  (update (response/redirect "/") :session dissoc :beeminder))

(defn get-configured-goal
  "if the goal is configred, return it even if it has a new slug name"
  [user goal-slug]
  (if-let [goal-with-slug-in-db (goal/get-by-beeminder-slug user goal-slug)]
    goal-with-slug-in-db
    (let [beeminder-goal (beeminder/goal-info user goal-slug)]
      (first (for [goal (goal/get-by-user user)
                   :when (= (:id beeminder-goal)
                            (:beeminder-id goal))]
               goal)))))

(defn add-datapoint
  [beeminder-username slug]
  (log/info "Adding a datapoint"
            {:beeminder-username beeminder-username :slug slug})
  (let [user (user/get beeminder-username)
        value (-> user
                  :wanikani-api-key
                  maintained-progress)]
    (if-let [goal (get-configured-goal user slug)]
      (do
        ;; add the datapoint
        (beeminder/add-datapoint user slug {:value value})
        ;; if the slug has changed, updated it
        (when (not= slug (:beeminder-slug goal))
          (goal/update-beeminder-slug! goal slug))
        true)
      ;; if we couldn't get the goal, log an error message, and do not update
      (do
        (log/info "Goot hook for goal that wasn't set up with WaniKani Minder"
                  {:beeminder-username beeminder-username :slug slug})
        false))))

;; # handlers

(defroutes site-routes
  ;; new
  (GET "/" {session :session} (homepage session nil))
  (POST "/" {:keys [session params]} (homepage session params))
  (GET "/auth/beeminder/callback" [access_token username error error_description
                                   :as {session :session}]
       (if-not error
         (login session access_token username)
         (pages/error error error_description)))
  (GET "/auth/logout" {session :session} (logout session))
  (route/not-found (pages/error "not found" "No page found with this URL")))

(defroutes api-routes
  (POST "/hooks/beeminder/autofetch" [username slug]
        (if (add-datapoint username slug)
          {:body {:success true}
           :status 200}
          {:body {:success false
                  :error "tried to update goal not managed by this app"}
           :status 400})))

(def redact-keys
  "the defaults plus access_token"
  #{:access_token :authorization :password :token :secret :secret-key :secret-token})

(def app
  (routes (-> site-routes
              (wrap-defaults site-defaults)
              (logger/wrap-with-logger {:redact-key? redact-keys}))
          (-> api-routes
              (logger/wrap-with-logger {:redact-key? redact-keys})
              (json/wrap-json-response)
              (wrap-defaults api-defaults))))
