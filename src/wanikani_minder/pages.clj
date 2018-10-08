(ns wanikani-minder.pages
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [ring.util.anti-forgery :as ring-af]))

(def explanation
  [:div
   [:h2 "What this is"]
   [:p "The is an integration to automatically track " [:a {:href "http://wanikani.com"} "WaniKani"] " progress with a " [:a {:href "http://beeminder.com"} "Beeminder"] " goal."]
   [:p "The number tracked is the number of items that you have started studying and are caught up on reviews for; that is, they are not currently due. This is the number that represents real progress; if you only track the number you've started, then when you fall behind on reviews your number stays high but you might have actually forgotten everything and not made any true progress. This number is also useful when you're trying to work down a backlog of due items, since it's measuring the increase in caught up cards."]
   [:p "It uses the highest number it gets for the day, so if you make sure you're on track, new items coming due will not cause you to derail later in the day."]])

(defn logged-out-homepage
  [beeminder-authorize-url]
  (html [:div
         [:h1 "WaniKani Minder"]
         [:p [:a {:href beeminder-authorize-url} "Login via beeminder"]]
         explanation]))

(defn error-span
  [message]
  [:span {:style "color:red"}
   "Error: "
   (if (string? message)
     message
     (string/join ", " message))])

(defn logged-in-homepage
  [{:keys [beeminder-id wanikani-api-key]}
   {:keys [create-goal wanikani]}]
  (html [:div
         [:h1 "WaniKani Minder"]
         [:p "Welcome, " beeminder-id]
         [:p [:a {:href "/auth/logout"} "Log out"]]
         [:h2 "WaniKani settings"]
         [:form {:method :post}
          (if-let [username (:username wanikani)]
            [:p {:style "color:green"} "Stored token for WaniKani user " username])
          (if-let [e (:error wanikani)]
            [:p (error-span "Invalid token. Are you sure it is your current API v1 (not v2) token?")])
          (ring-af/anti-forgery-field)
          [:p [:label {:for "wanikani-api-key"} "WaniKani v1 API key: "]
           [:input {:type :text :id "wanikani-api-key" :name "wanikani-api-key"}]
           " currently " (if wanikani-api-key wanikani-api-key "unset")
           [:br]
           "You can find this in " [:a {:href "https://www.wanikani.com/settings/account"} "WaniKani's settings"]]
          [:p [:button {:type :submit :name "action" :value "wanikani"} "Update"]]]
         [:h2 "Create Beeminder goal"]
         (if-not wanikani-api-key
           [:p "You must have a WaniKani API key set in order to create a goal."]
           [:form {:method :post}
            (if-let [e (:unexpected (:errors create-goal))]
              [:p (error-span (str "Unexpected error creating goal: " e))])
            (if (:success create-goal)
              [:p {:style "color:green"} "Created goal " (:slug create-goal)])
            (ring-af/anti-forgery-field)
            [:p [:label {:for "slug"} "Goal name: "]
             [:input {:type :text :id "slug" :name "slug"}]
             " "
             (if-let [e (:slug (:errors create-goal))] (error-span e))]
            [:p [:label {:for "rate"} "Daily rate: "]
             [:input {:type :text :id "rate" :name "rate"}]
             " "
             (if-let [e (:rate (:errors create-goal))] (error-span e))]
            [:p [:button {:type :submit :name "action" :value "create-goal"} "Create"]]])
         explanation]))

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
