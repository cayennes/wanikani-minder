(ns wanikani-url-minder.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.codec :refer [url-encode]]
            [clj-http.client :as client]
            [wanikani-minder.pages :as pages]))

;; # stuff

;; initialization

(def config
  {:beeminder-client-secret (System/getenv "BEEMINDER_CLIENT_SECRET")
   :beeminder-client-id (System/getenv "BEEMINDER_CLIENT_ID")
   :base-url (System/getenv "BASE_URL")})

(assert (every? some? (keys config)))

;; auth

(defn redirect-uri
  []
  (str (:base-url config) "auth/beeminder/callback"))

(defn authorize-url
  []
  (str "https://www.beeminder.com/apps/authorize?"
       "client_id=" (:beeminder-client-id config)
       "&redirect_uri=" (url-encode (redirect-uri))
       "&response_type=token"))

;; # legacy urlminder hack

;; wanikani access

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

;; make urlminder pages

(defn n-word-string
  [n]
  (->> (cons n (repeat "w"))
       (take n)
       (clojure.string/join " ")))

;; # handler

(defroutes app-routes
  ;; new
  (GET "/" [] (pages/landing (authorize-url)))
  (GET "/auth/beeminder/callback" [access_token username error error_description]
       (if-not error
         (pages/beeminder-login access_token username)
         (pages/error error error_description)))
  ;; old
  (GET "/wanikani-urlminder" [] pages/legacy-intro)
  (GET "/wanikani-urlminder/user/:wanikani-key/backlog-reduction-from/:starting-due"
       [wanikani-key starting-due]
       (n-word-string (- (Integer/parseInt starting-due)
                         (get-due-count wanikani-key))))
  (GET "/wanikani-urlminder/user/:wanikani-key/total-studied"
       [wanikani-key]
       (n-word-string (get-total wanikani-key)))
  (GET "/wanikani-urlminder/user/:wanikani-key/maintained-progress"
       [wanikani-key]
       (n-word-string (- (get-total wanikani-key) (get-due-count wanikani-key))))
  (route/not-found (pages/error "not found" "No page found with this URL")))

(def app
  (wrap-defaults app-routes site-defaults))
