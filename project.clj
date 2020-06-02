(defproject wanikani-minder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-logger "1.0.1"]
                 [clj-http "3.10.1"]
                 [cheshire "5.10.0"]
                 [hiccup "1.0.5"]
                 [org.postgresql/postgresql "42.2.12"]
                 [ragtime "0.8.0"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.carouselapps/to-jdbc-uri "0.5.0"]]

  :plugins [[lein-ring "0.12.5"]]

  :ring {:handler wanikani-minder.handler/app}

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [peridot "0.5.3"]]
         :ring {:nrepl {:start? true}}}}

  :aliases {"migrate" ["run" "-m" "wanikani-minder.migrations/migrate"]
            "rollback" ["run" "-m" "wanikani-minder.migrations/rollback"]})
