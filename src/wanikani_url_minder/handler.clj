(ns wanikani-url-minder.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clj-http.client :as client]
            [hiccup.core :refer [html]]))

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

;; page making

(defn n-word-string
  [n]
  (apply str (repeat n "w ")))

(def intro-page
  (html [:div
         [:h1 "WaniKani URLminder"]
         [:p "Automatically beemind WaniKani progress via a URLminder goal using one of these special urls.  You can find you WaniKani API key in settings."]
         [:h2 "Reducing a large review queue"]
         [:p "Since URLminder goals only count up, beemind clearing out a large review backlog by the amount reduced.  Include the starting size in the URL and make it your goal target."]
         [:code "https://web-glue.herokuapp.com/wanikani-urlminder/user/"
          [:span {:style "color:green;"} "[insert WaniKani API key here]"]
          "/backlog-reduction-from/"
          [:span {:style "color:green;"} "[insert starting count here]"]]
         [:h2  "Total studied items"]
         [:p "The number of different items that you've started reviewing."]
         [:p "It's probably a terrible idea to beemind this; make it a modest goal keep a good buffer if you do in case you don't unlock lessons in time."]
         [:code "https://web-glue.herokuapp.com/wanikani-urlminder/user/"
          [:span {:style "color:green;"} "[insert WaniKani API key here]"]
          "/total-studied"]
         [:h2 "Maintained progress"]
         [:p "Total studied minus total due."]
         [:code "https://web-glue.herokuapp.com/wanikani-urlminder/user/"
          [:span {:style "color:green;"} "[insert WaniKani API key here]"
           "/maintained-progress"]]]))

;; handler

(defroutes app-routes
  (GET "/" [] intro-page)
  (GET "/wanikani-urlminder" [] intro-page)
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
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
