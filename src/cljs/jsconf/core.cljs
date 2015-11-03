(ns jsconf.core
  (:require [om.core :as om]
            [sablono.core :refer-macros [html]]
            [jsconf.state :refer [state]]))

(enable-console-print!)

(defn widget [data owner]
  (reify
      om/IRender
    (render [this]
      (html
       [:h1 (:text data)]))))

(om/root widget state
         {:target (. js/document (getElementById "app"))})

