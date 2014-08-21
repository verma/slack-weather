(ns slack-weather.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.core.async :refer [thread]]))

(def hook-url "https://mazira.slack.com/services/hooks/incoming-webhook?token=CzlgcnVk25WSbxYpzu2qYVos")


(defn pull-values [m val-map]
  (into {} (for [[k v] val-map]
             [k (get-in m (if (sequential? v) v [v]))])))

(defn weather-for-zip [zip]
  (-> (str "http://api.openweathermap.org/data/2.5/find?q="
           zip
           ",USA&units=imperial")
      client/get
      :body
      json/read-str
      (pull-values {:name ["list" 0 "name"]
                    :temp ["list" 0 "main" "temp"]
                    :humidity ["list" 0 "main" "humidity"]
                    :temp-min ["list" 0 "main" "temp_min"]
                    :temp-max ["list" 0 "main" "temp_max"]
                    :conditions ["list" 0 "weather" 0 "main"]})))

(defn weather-to-str [w]
  (str (:name w) " " (:temp w) "F " (:conditions w)
       ", Min: " (:temp-min w) "F, Max: " (:temp-max w) "F"
       ", Humidity: " (:humidity w)))


(defn post-to-slack [url msg]
  (let [m (merge {:username "SASSY BOT"
                  :icon_emoji ":feelsgood:"} msg)]
    (client/post url {:body (json/write-str m)
                      :content-type :json})))

(defn weather-to-slack [zip]
  (let [weather (-> zip
                    weather-for-zip
                    weather-to-str)]
    (post-to-slack hook-url {:text weather
                             :icon_emoji ":cloud:"})))

(def auth-token "FNQd36LfYAl6w9gLaw56nteU")

(defn check-zip [zip]
  (re-matches #"^\d{5}$" (clojure.string/trim zip)))

(defroutes app-routes
  (POST "/slack" {:keys [params] :as request}
        (if (and (= "/weather" (:command params))
                 (= auth-token (:token params))
                 (check-zip (:text params)))
          (do
            (thread (weather-to-slack (clojure.string/trim (:text params))))
            {:status 200
             :content-type "text/plain"
             :body "Getting weather information, please wait."})
          {:status 405
           :content-type "text/plain"
           :body "You need to provide a valid zip code"}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
