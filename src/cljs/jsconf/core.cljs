(ns jsconf.core
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [qrcloj.core :refer [make-symbol]]
            [figwheel.client :as fig]
            [jsconf.state :refer [state]]))

(enable-console-print!)

(def slide? (-> js/location (aget "hash") (= "#slide")))
(def origin (-> js/location .-origin))


(defcomponent qr-widget [{:keys [size ecl]} _]
  (did-mount [_]
    (println "did-mount called")
    (make-symbol "qr" ecl origin))
  (did-update [_ _ _]
    (println "did-update called")
    (make-symbol "qr" ecl origin))
  (render [_]
    (println "render called")
    (html
     [:div
      [:h3 origin]
      [:canvas#qr {:width (str size "px")
                   :height (str size "px")}]]
    )))


(defcomponent app-widget [{:keys [title connected qr]} _]
  (render [_]
    (html
      [:div.wrapper
       [:h1 title]
       (if slide?
         [:div
          [:h2 (str "Connected: " connected)]
          (om/build qr-widget qr)]
         )]
      )))

(def root (. js/document (getElementById "app")))
(om/root app-widget state {:target root})

(fig/add-message-watch
 :state-server
 (fn [{:keys [msg-name] :as msg}]
   (when (= msg-name :push-state)
     (println "raw:" msg)
     (reset! state (:state msg)))))
