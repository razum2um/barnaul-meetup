(ns jsconf.core
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.system :as sys]
            [compojure.core :refer [routes GET POST defroutes]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-params]]
            [jsconf.state :refer [state]])
  (:gen-class))

(defroutes app-routes
  (GET "/test" req (str "<html><body><pre>req:" (with-out-str (clojure.pprint/pprint req)) "</pre></body></html>"))
  (POST "/vote" {{v :v :as params} :params :as req}
        (pr-str params)))

(def app (-> #'app-routes
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
