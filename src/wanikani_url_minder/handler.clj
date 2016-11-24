(ns wanikani-url-minder.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clj-http.client :as client]
            [hiccup.core :refer [html]]))

;; wanikani access

(defn study-queue-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/user/%s/study-queue" wanikani-key))

(defn srs-distribution-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/user/%s/srs-distribution" wanikani-key))


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

;; page making

(defn n-word-string
  [n]
  (apply str (repeat n "w ")))

(def intro-page
  (html [:p "Sorry, no documentation yet"]))

;; handler

(defroutes app-routes
  (GET "/" [] intro-page)
  (GET "/v1/user/:wanikani-key/backlog-reduction-from/:starting-due"
       [wanikani-key starting-due]
       (n-word-string (- (Integer/parseInt starting-due)
                         (get-due-count wanikani-key))))
  (GET "/v1/user/:wanikani-key/items-ever-studied"
       [wanikani-key]
       (n-word-string (get-total wanikani-key)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
