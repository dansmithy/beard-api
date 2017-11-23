(defproject beard-api "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [yogthos/config "0.9"]
                 [clj-http "3.7.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :main beard-api.handler
  :aot [beard-api.handler]
  :ring {:handler beard-api.handler/app
         :port 5000}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
