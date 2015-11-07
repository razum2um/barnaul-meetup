(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.system :as sys]
            [jsconf.state :refer [state]])
  (:gen-class))

(defn dev-system []
  (component/system-map
   :css-watcher (sys/css-watcher {:watch-paths ["resources/public/css"]})
   :figwheel-system (sys/figwheel-system (sys/fetch-config))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
