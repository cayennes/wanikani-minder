(ns wanikani-minder.wanikani
  (:require [clj-http.client :as client]
            [clojure.string :as string])
  (:import (java.time Instant)))

(defn api-key-version
  [api-key & _args]
  (cond
    (nil? api-key) nil
    (string/includes? api-key "-") :v2
    :else :v1))

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

;; v2

(defn auth-header
  [wanikani-key]
  {:authorization (str "Bearer " wanikani-key)})

(defn due?
  [review-summary-object]
  (neg? (.compareTo (Instant/parse (:available_at review-summary-object))
                    (Instant/now))))

(defmethod get-due-count :v2
  [wanikani-key]
  (-> (client/get "https://api.wanikani.com/v2/summary"
                  {:headers (auth-header wanikani-key)
                   :as :json})
      (get-in [:body :data :reviews])
      (->> (filter due?)
           (mapcat :subject_ids))
      (count)))

(defmethod get-total :v2
  [wanikani-key]
  (get-in (client/get "https://api.wanikani.com/v2/assignments"
                      {:headers (auth-header wanikani-key)
                       :as :json
                       :query-params {:started true
                                      :hidden false}})
          [:body :total_count]))

(defmethod get-username-or-error :v2
  [wanikani-key]
  (let [result (client/get "https://api.wanikani.com/v2/user"
                           (merge {:headers (auth-header wanikani-key)
                                   :as :json
                                   :throw-exceptions false}))]
    (if-let [username (get-in result [:body :data :username])]
      {:username username
       :success true}
      {:error (str "status " (:status result))
       :success false})))
