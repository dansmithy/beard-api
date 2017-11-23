(ns beard-api.handler
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as json]
            [config.core :as config.core]
            [clojure.string :as string])
  (:import [java.net InetAddress UnknownHostException]))

(defonce env
  (merge
   {:port "5000"}
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
    {:status 200
     :body {:styles beard-styles}})

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
