(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [figwheel-sidecar.system :as sys]
            [compojure.core :refer [routes GET POST defroutes]]
            [compojure.response :refer [render]]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.components.figwheel-server :as server]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [set-cookie]]
            [jsconf.state :refer [state update-answers]])
  (:gen-class))

(defn with-client-id [response {:keys [cookies]}]
  (let [client-id (or (some-> cookies (get "client-id") :value Integer/parseInt)
                      (rand-int 10000))]
    (set-cookie response "client-id" client-id)))

(defroutes app-routes
  (POST "/vote" {{v :v} :params cookies :cookies :as req}
        (-> (update-answers
             (some-> cookies (get "client-id") :value Integer/parseInt)
             v)
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

(defn get-connections [figwheel-system]
  (-> figwheel-system :system deref :figwheel-server :connection-count))

(defrecord PushStateService [figwheel-system]
  component/Lifecycle
  (start [comp]

    (remove-watch state ::state)
    (add-watch state ::state
               (fn [_ _ _ new-state]
                 (server/send-message
                  figwheel-system
                  ::server/broadcast
                  {:msg-name :push-state :state new-state})))

    (remove-watch state ::conn)
    (add-watch (get-connections figwheel-system)
               ::conn
               (fn [_ _ _ new-state]
                 (swap! state assoc-in [:connected] (get new-state "dev"))))

    (swap! state assoc-in [:connected] (-> figwheel-system get-connections (get "dev")))

    comp)
  (stop [comp]
    comp))

(defn dev-system []
  (component/system-map
   :css-watcher (sys/css-watcher {:watch-paths ["resources/public/css"]})
   :figwheel-system (sys/figwheel-system (-> (sys/fetch-config)
                                             (assoc-in [:figwheel-options :resolved-ring-handler] app)))
   :state-pusher (component/using (map->PushStateService {}) [:figwheel-system])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
