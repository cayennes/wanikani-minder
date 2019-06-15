(defproject wanikani-minder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [clj-http "3.6.1"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]
                 [org.postgresql/postgresql "42.2.2"]
                 [ragtime "0.7.1"]
                 [com.layerware/hugsql "0.4.8"]
                 [com.carouselapps/to-jdbc-uri "0.5.0"]]

  :plugins [[lein-ring "0.9.7"]]

  :ring {:handler wanikani-minder.handler/app
         ;; :nrepl {:start? true}
         }

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]}
   :test {:dependencies [[peridot "0.5.1"]]}}

  :aliases {"migrate" ["run" "-m" "wanikani-minder.migrations/migrate"]
            "rollback" ["run" "-m" "wanikani-minder.migrations/rollback"]})
