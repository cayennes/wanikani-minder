(ns wanikani-minder.pages
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :as ring-af]))

(defn logged-out-homepage
  [beeminder-authorize-url]
  (html [:div
         [:h1 "WaniKani Minder"]
         [:p "Automatically beemind WaniKani progress"]
         [:p [:a {:href beeminder-authorize-url} "Login via beeminder"]]]))

(defn logged-in-homepage
  [{:keys [beeminder-id wanikani-api-key beeminder-goal-slug]}]
  (html [:div
         [:h1 "WaniKani Minder"]
         [:p "Welcome, " beeminder-id]
         [:p [:a {:href "/auth/logout"} "Log out"]]
         [:h2 "WaniKani settings"]
         [:form {:method :post}
          (ring-af/anti-forgery-field)
          [:p [:label {:for "wanikani-api-key"} "WaniKani API key: "]
           [:input {:type :text :id "wanikani-api-key" :name "wanikani-api-key"}]
           " currently " (if wanikani-api-key wanikani-api-key "unset")
           [:br]
           "You can find this in " [:a {:href "https://www.wanikani.com/settings/account"} "WaniKani's settings"]]
          [:p [:button {:type :submit} "Update"]]]
         [:h2 "Create beeminder goal"]
         [:p "Coming soon: the ability to create a WaniKani maintained progress goal"]]))

(defn error
  [error error-description]
  (html [:div
         [:h1 "Error: " error]
         [:p error-description]]))

(defn legacy-intro
  []
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
