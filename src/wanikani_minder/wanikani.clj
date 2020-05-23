(ns wanikani-minder.wanikani
  (:require [clj-http.client :as client]
            [clojure.string :as string]))

(defn api-key-version
  [api-key & _args]
  (if (string/includes? api-key "-")
    :v2
    :v1))

(defmulti get-due-count api-key-version)
(defmulti get-total api-key-version)
(defmulti get-username-or-error api-key-version)

;; v1 API

(defn study-queue-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/study-queue" wanikani-key))

(defn srs-distribution-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/srs-distribution" wanikani-key))

(defn user-information-url
  [wanikani-key]
  (format "https://www.wanikani.com/api/v1/user/%s/user-information" wanikani-key))

(defmethod get-due-count :v1
  [wanikani-key]
  (get-in (client/get (study-queue-url wanikani-key)
                      {:as :json})
          [:body :requested_information :reviews_available]))

(defmethod get-total :v1
  [wanikani-key]
  (->> (client/get (srs-distribution-url wanikani-key)
                   {:as :json})
       :body
       :requested_information
       vals
       (map :total)
       (apply +)))

(defmethod get-username-or-error :v1
  [wanikani-key]
  (let [result (client/get (user-information-url wanikani-key)
                           {:as :json
                            :throw-exceptions false})]
    (if-let [username (get-in result [:body :user_information :username])]
      {:username username
       :success true}
      {:error (str "status " (:status result))
       :success false})))
