(ns jsconf.core
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [figwheel.client :as fig]
            [jsconf.state :refer [state]]))

(enable-console-print!)

(defcomponent app-widget [{:keys [title]} _]
  (render [_]
    (html
      [:div.wrapper
       [:h1 title]]
      )))

(def root (. js/document (getElementById "app")))
(om/root app-widget state {:target root})

(fig/add-message-watch
 :state-server
 (fn [{:keys [msg-name] :as msg}]
   (when (= msg-name :push-state)
     (println "raw:" msg)
     (reset! state (:state msg)))))
