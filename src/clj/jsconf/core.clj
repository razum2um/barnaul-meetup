(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [figwheel-sidecar.system :as sys]
            [compojure.core :refer [routes GET POST defroutes]]
            [compojure.response :refer [render]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [set-cookie]]
            [jsconf.state :refer [state]])
  (:gen-class))

(defn apply-vote [client-id vote-id {:keys [id] :as answer}]
  (if (= id vote-id)
    (update-in answer [:votes] #(conj % client-id))
    (update-in answer [:votes] #(disj % client-id))))

(defn update-votes [client-id vote-id votes]
  (mapv (partial apply-vote client-id vote-id) votes))

(defn with-client-id [response {:keys [cookies]}]
  (let [client-id (or (some-> cookies (get "client-id") :value Integer/parseInt)
                      (rand-int 10000))]
    (set-cookie response "client-id" client-id)))

(defroutes app-routes
  (GET "/test" req (str "<html><body><pre>req:" (with-out-str (clojure.pprint/pprint req)) "</pre></body></html>"))
  (POST "/vote" {{v :v :as params} :params cookies :cookies :as req}
        (pprint cookies)
        (-> (swap! state update-in [:answers] #(update-votes (some-> cookies (get "client-id") :value Integer/parseInt) v %))
            (render req)
            (with-client-id req)))
  (GET "/" req (-> "public/index.html"
                   io/resource
                   (render req)
                   (with-client-id req))))

(def app (-> #'app-routes
             wrap-json-response
             (wrap-defaults (assoc api-defaults :cookies true))
             wrap-json-params
             wrap-reload))

(defn dev-system []
  (component/system-map
   :figwheel (sys/figwheel-system (-> (sys/fetch-config)
                                      (assoc-in [:figwheel-options :resolved-ring-handler] app)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
