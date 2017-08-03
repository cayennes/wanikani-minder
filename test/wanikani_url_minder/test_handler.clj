(ns wanikani-url-minder.test-handler
  (:require [wanikani-url-minder.handler :as handler]
            [clojure.test :refer (deftest is)]))

(deftest make-n-word-string-works
  (is (= "5 w w w w" (handler/n-word-string 5)))
  (is (= "" (handler/n-word-string 0))))
