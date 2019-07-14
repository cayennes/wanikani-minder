(ns wanikani-minder.wanikani
  (:require [clj-http.client :as client]))

(defn study-queue-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/study-queue" wanikani-key))

(defn srs-distribution-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/srs-distribution" wanikani-key))

(defn user-information-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/user-information" wanikani-key))

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

(defn get-username-or-error
  [wanikani-key]
  (let [result (client/get (user-information-url wanikani-key)
                           {:as :json
                            :throw-exceptions false})]
    (if-let [username (get-in result [:body :user_information :username])]
      {:username username
       :success true}
      {:error (str "status " (:status result))
       :success false})))
