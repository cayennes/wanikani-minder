(ns wanikani-minder.main
  (:require [wanikani-minder.handler :as handler]
            [wanikani-minder.config :refer [config]]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main [& [port]]
  (let [port (Integer. (or port (:port config) 3000))]
    (jetty/run-jetty handler/app {:port port :join? false})))
