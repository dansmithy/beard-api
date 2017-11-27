(ns beard-api.handler
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as json]
            [config.core :as config.core]
            [clojure.string :as string]
            [beard-api.moustache :as moustache]
            [clojure.java.io :as io])
  (:import [java.net InetAddress UnknownHostException]))

(def last-tweet-file "last_tweet")
(def last-poll-run-date "last_run_date")

(defonce env
  (merge
   {:port "5000"
    :tweet-directory "/tweet"}
   config.core/env))

(def beard-styles
  [{:name "Balbo"}
   {:name "Bandholz"}
   {:name "Clean shaven"}
   {:name "Circle beard"}
   {:name "Extended goatee"}
   {:name "Friendly mutton chops"}
   {:name "Full beard"}
   {:name "Garibaldi"}])

(defn read-latest-tweet
  []
  (slurp (str (env :tweet-directory) "/" last-tweet-file)))

(defn read-poll-run-date
  []
  (slurp (str (env :tweet-directory) "/" last-poll-run-date)))

(defn get-ip-address
  []
  (str (InetAddress/getLocalHost)))

(defn- add-to-host-sequence
  [host-sequence]
  (string/join " "
            (cons (get-ip-address)
                  (string/split (or host-sequence "") #" "))))

(defn hostname-middleware
  [handler]
  (fn [request]
    (let [{{host-sequence "X-Host-Sequence" :as headers} :headers :as response} (handler request)]
      (assoc response :headers
             (assoc headers "X-Host-Sequence" (add-to-host-sequence host-sequence))))))

(defroutes app-routes
  (GET "/" []
    (let [moustache-response (moustache/moustache-request)]
      {:status 200
       :headers {"X-Host-Sequence" (:host moustache-response)}
       :body {:styles beard-styles
              :moustache-styles (:styles (:data moustache-response))
              :tweet
              {:text (read-latest-tweet)
               :poll-date (read-poll-run-date)}}}))

  (GET "/request" request
      {:status 200
       :body (select-keys request [:headers
                                   :server-port
                                   :server-name
                                   :remote-addr
                                   :uri
                                   :query-string
                                   :scheme
                                   :request-method])})

  (GET "/address" []
    {:status 200
     :body {:address (get-ip-address)}})

  (GET "/env" []
    {:status 200
     :body (into (sorted-map) config.core/env)})

  (route/not-found "Not Found"))

(def app
  (->
   app-routes
   (hostname-middleware)
   (json/wrap-json-response)
   (json/wrap-json-body {:keywords? true})
   (wrap-defaults site-defaults)))


(defn -main [& args]
  (run-jetty #'app {:port (Integer/parseInt (:port env)) :join? true}))
