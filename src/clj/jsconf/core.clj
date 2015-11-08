(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.system :as sys]
            [figwheel-sidecar.components.figwheel-server :as server]
            [jsconf.state :refer [state]])
  (:gen-class))

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
    comp)
  (stop [comp]
    comp))

(defn dev-system []
  (component/system-map
   :css-watcher (sys/css-watcher {:watch-paths ["resources/public/css"]})
   :figwheel-system (sys/figwheel-system (sys/fetch-config))
   :state-pusher (component/using (map->PushStateService {}) [:figwheel-system])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
