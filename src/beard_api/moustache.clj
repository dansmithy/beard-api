(ns beard-api.moustache
  (:require [config.core :as config.core]
            [clj-http.client :as http]))

(def timeout-in-ms 1000)

(defn moustache-request
  []
  (try
    (let [url (format "http://%s"
                      (config.core/env :moustache-api-base-url "moustache-api.default.svc.cluster.local:30001"))
          response
          (http/request
           {:method :get
            :url url
            :as :json
            :socket-timeout timeout-in-ms
            :conn-timeout timeout-in-ms})]
      {:data (:body response)
       :host (-> response :headers (get "X-Host-Sequence"))})
    (catch Exception e
      (do
        (println "Failed to get response from Moustache API" e)
        {:data {:styles [{:name "<canned>"}]}}))))
