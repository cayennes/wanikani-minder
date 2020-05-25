(defproject wanikani-minder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :main wanikani-minder.main

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [clj-http "3.10.0"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]
                 [org.postgresql/postgresql "42.2.2"]
                 [ragtime "0.7.1"]
                 [com.layerware/hugsql "0.4.8"]
                 [com.carouselapps/to-jdbc-uri "0.5.0"]]

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [peridot "0.5.1"]]
         :ring {:nrepl {:start? true}}}}


  :aliases {"migrate" ["run" "-m" "wanikani-minder.migrations/migrate"]
            "rollback" ["run" "-m" "wanikani-minder.migrations/rollback"]})
