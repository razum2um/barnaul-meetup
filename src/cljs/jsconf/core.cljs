(ns jsconf.core
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [ajax.core :refer [GET POST]]
            [qrcloj.core :refer [make-symbol]]
            [jsconf.state :refer [state]]))

(enable-console-print!)

(defcomponent qr-widget [{:keys [size ecl value]} owner]
  (did-mount [_]
   (println "did-mount called")
   (make-symbol "qr" ecl value))
  (did-update [_ _ _]
   (println "did-update called")
   (make-symbol "qr" ecl value))
  (render [_]
   (println "render called")
   (html
    [:canvas#qr {:width (str size "px")
                 :height (str size "px")}])))

(defcomponent widget [{:keys [title qr]} owner]
  (render [_]
   (html
    [:div.wrapper
     [:h1 title]
     [:div (om/build qr-widget qr)]])))

(defn echo-post [url params]
  (POST url {:params params
             :format :json
             :handler println}))

(defn update-state [value & path]
  (println "prev state:" (pr-str @state))
  (swap! state #(assoc-in % (map keyword path) value))
  (println "new state:" (pr-str @state)))

(def root (. js/document (getElementById "app")))
(om/root widget state {:target root})

