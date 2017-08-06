(ns wanikani-minder.test-handler
  (:require [wanikani-minder.handler :as handler]
            [clojure.test :refer (deftest is)]
            [ring.mock.request :as mock]
            [clj-http.client :as client]))

(deftest make-n-word-string-works
  (is (= "5 w w w w" (handler/n-word-string 5)))
  (is (= "" (handler/n-word-string 0))))

(deftest routes-return-success
  (with-redefs [handler/get-total (constantly 17)
                handler/get-due-count (constantly 4)
                client/get (fn [& _] (assert false "no internet allowed"))
                client/post (fn [& _](assert false "no internet allowed"))]
    (doseq [path-etc ["/"
                      "/auth/beeminder/callback?access_token=a&username=b"
                      "/wanikani-urlminder"
                      "/wanikani-urlminder/user/somekey/backlog-reduction-from/20"
                      "/wanikani-urlminder/user/somekey/total-studied"
                      "/wanikani-urlminder/user/somekey/maintained-progress"]]
      (is (= 200 (:status (handler/app (mock/request :get path-etc))))))))