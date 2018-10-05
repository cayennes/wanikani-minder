(ns wanikani-minder.handler
  (:require [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.util.codec :refer [url-encode]]
            [ring.util.response :as response]
            [ring.middleware.json :as json]
            [clj-http.client :as client]
            [wanikani-minder.beeminder :as beeminder]
            [wanikani-minder.config :refer [config]]
            [wanikani-minder.pages :as pages]
            [wanikani-minder.db.user :as user]))

;; # stuff

;; wanikani access
;; TODO: move to own namespace

(defn study-queue-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/study-queue" wanikani-key))

(defn srs-distribution-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/srs-distribution" wanikani-key))

(defn get-due-count
  [wanikani-key]
  (get-in (client/get (study-queue-url wanikani-key)
                      {:as :json})
          [:body :requested_information :reviews_available]))

(defn get-total
  [wanikani-key]
  (->> (client/get (srs-distribution-url wanikani-key)
                   {:as :json})
       :body
       :requested_information
       vals
       (map :total)
       (apply +)))

(defn maintained-progress
  [wanikani-key]
  (- (get-total wanikani-key) (get-due-count wanikani-key)))

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

(defn homepage
  [session]
  (if-let [username (get-in session [:beeminder :username])]
    (pages/logged-in-homepage (user/get username))
    (pages/logged-out-homepage (authorize-url))))

(defn login
  [session access-token username]
  (user/beeminder-create-or-update! {:username username :access-token access-token})
  (assoc-in (response/redirect "/")
            [:session :beeminder] {:username username :token access-token}))

(defn logout
  [session]
  (update (response/redirect "/") :session dissoc :beeminder))

(defn add-datapoint
  [beeminder-username slug]
  ;; TODO: check that the user does actually have that goal configured
  (let [user (user/get beeminder-username)
        value (-> user
                  :wanikani-api-key
                  maintained-progress)]
    (beeminder/add-datapoint user slug {:value value})))

;; # legacy urlminder hack

;; make urlminder pages

(defn n-word-string
  [n]
  (->> (cons n (repeat "w"))
       (take n)
       (clojure.string/join " ")))

;; # handlers

(defroutes site-routes
  ;; new
  (GET "/" {session :session} (homepage session))
  (POST "/"  [wanikani-api-key :as {:keys [session]}]
        (let [beeminder-username (get-in session [:beeminder :username])]
          (when (not-empty wanikani-api-key)
            (user/update-wanikani-token! beeminder-username wanikani-api-key))
          (homepage session)))
  (GET "/auth/beeminder/callback" [access_token username error error_description
                                   :as {session :session}]
       (if-not error
         (login session access_token username)
         (pages/error error error_description)))
  (GET "/auth/logout" {session :session} (logout session))
  ;; old
  (GET "/wanikani-urlminder" [] (pages/legacy-intro))
  (GET "/wanikani-urlminder/user/:wanikani-key/backlog-reduction-from/:starting-due"
       [wanikani-key starting-due]
       (n-word-string (- (Integer/parseInt starting-due)
                         (get-due-count wanikani-key))))
  (GET "/wanikani-urlminder/user/:wanikani-key/total-studied"
       [wanikani-key]
       (n-word-string (get-total wanikani-key)))
  (GET "/wanikani-urlminder/user/:wanikani-key/maintained-progress"
       [wanikani-key]
       (n-word-string (maintained-progress wanikani-key)))
  (route/not-found (pages/error "not found" "No page found with this URL")))

(defroutes api-routes
  (POST "/hooks/beeminder/autofetch" [username slug]
        (add-datapoint username slug)
        (response/response {:result "success"})))

(def app
  (-> (routes api-routes
              (wrap-defaults site-routes site-defaults))
      (json/wrap-json-response)
      (wrap-defaults api-defaults)))
