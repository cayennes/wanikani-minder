(ns wanikani-minder.test-handler
  (:require [wanikani-minder.handler :as handler]
            [clojure.test :refer (deftest is)]
            [ring.mock.request :as mock]
            [clj-http.client :as client]))

;; see http://mcramm.com/post/integration-tests-for-clojure-and-postgres/ for
;; one good approach when it would be useful to add smoke tests that interact
;; with the database.
;;
;; when doing that, switch to running with circleci.test so that we can run
;; individual tests with fixtures

(deftest homepage-returns-success
  (with-redefs [client/get (fn [& _] (assert false "no internet allowed"))
                client/post (fn [& _] (assert false "no internet allowed"))]
    (doseq [path-etc ["/"]]
      (is (= 200 (:status (handler/app (mock/request :get path-etc))))))))
