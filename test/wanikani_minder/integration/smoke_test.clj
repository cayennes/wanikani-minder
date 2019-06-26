(ns wanikani-minder.integration.smoke-test
  "end to end tests of the very most basic functionality of the very most basic happy paths"
  (:require [wanikani-minder.handler :as handler]
            [wanikani-minder.db.user :as user]
            [clojure.test :refer (deftest is)]
            [peridot.core :as peridot]
            [clj-http.client :as client]))

(deftest logged-out-homepage
  (with-redefs [client/get (fn [& _] (assert false "no internet allowed"))
                client/post (fn [& _] (assert false "no internet allowed"))]
    (is (= 200
           (-> (peridot/session handler/app)
               (peridot/request "/" :request-method :get)
               (get-in [:response :status]))))))

(deftest log-in
  (with-redefs [client/get (fn [& _] (assert false "no internet allowed"))
                client/post (fn [& _] (assert false "no internet allowed"))
                user/beeminder-create-or-update! identity
                user/get identity]
    (let [res (-> (peridot/session handler/app)
                  ;; log in - we skip right to what the oath redirect would give us
                  (peridot/request "/auth/beeminder/callback"
                                   :request-method :get
                                   :params {:access_token "shrug-guitar"
                                            :username "kick-oranges"})
                  (peridot/follow-redirect)
                  :response)]
      (is (= 200 (:status res)))
      ;; Look for log out text to check whether we're logged in
      (is (re-find #"Log out" (:body res))))))
