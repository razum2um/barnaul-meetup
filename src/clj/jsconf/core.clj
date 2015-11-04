(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [figwheel-sidecar.system :as sys]
            [compojure.core :refer [routes GET POST defroutes]]
            [compojure.response :refer [render]]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.components.figwheel-server :as server]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [set-cookie]]
            [clojure.core.async :refer [go-loop timeout <!]]
            [jsconf.state :refer [state]])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream])
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

(defn update-state [client-id v]
  (swap! state update-in [:answers] #(update-votes client-id v %)))

(defroutes app-routes
  (GET "/test" req (str "<html><body><pre>req:" (with-out-str (clojure.pprint/pprint req)) "</pre></body></html>"))
  (POST "/vote" {{v :v :as params} :params cookies :cookies :as req}
        (-> (update-state
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

(defrecord PushStateService [figwheel-system]
  component/Lifecycle
  (start [{:keys [state-server] :as this}]

    (remove-watch state ::state)
    (add-watch state ::state
               (fn [_ _ _ new-state]
                 (server/send-message
                  figwheel-system
                  ::server/broadcast
                  {:msg-name :push-state :state new-state})))

    this
    #_(if-not state-server
      (let [state-server (atom true)]
        (go-loop []
          (when @state-server
            (server/send-message figwheel-system
                                 ::server/broadcast
                                 {:msg-name :time-push :time (java.util.Date.)})
            (<! (timeout 1000))
            (recur)))
        (assoc this :state-server state-server))
      this))
  (stop [{:keys [state-server] :as this}]
    (if state-server
      (do (reset! state-server false)
          (assoc this :state-server nil))
      this)))

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
