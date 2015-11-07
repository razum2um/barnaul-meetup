(ns jsconf.core
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
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
