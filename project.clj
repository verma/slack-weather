(defproject slack-weather "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [clj-http "1.0.0"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler slack-weather.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
