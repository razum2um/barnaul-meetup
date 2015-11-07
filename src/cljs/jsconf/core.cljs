(ns jsconf.core
  (:require [om.core :as om]
            [goog.net.cookies]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [ajax.core :refer [GET POST]]
            [qrcloj.core :refer [make-symbol]]
            [figwheel.client :as fig]
            [jsconf.state :refer [state]]))

(enable-console-print!)

(def client-id (int (.get goog.net.cookies "client-id")))
(def slide? (-> js/location (aget "hash") (= "#slide")))
(def origin (-> js/location .-origin))

(defcomponent stat-widget [{:keys [text votes]} _]
  (render
   [_]
   (html
    [:div.col-md-6.col-sm-12
     [:div (count votes)]
     [:div text]])))

(defn vote! [id]
  (POST "/vote" {:params {:v id}
                 :format :json
                 :handler println}))

(defn btn-default [id text]
  {:type "button"
   :class "btn btn-primary btn-lg"
   :value text
   :on-click (fn [_] (vote! id))})

(defn btn-chosen [id text count]
  {:type "button"
   :class "btn btn-success btn-lg"
   :value (str text ": +" count)
   :on-click (fn [_] (vote! id))})

(defcomponent answer-widget [{:keys [id text votes]} _]
  (render [_]
   (html
    [:div.col-md-6.col-sm-12
     (if true [:p (pr-str votes)])
     [:input (if-let [my-votes (-> (group-by identity votes) (get client-id))]
               (btn-chosen id text (count my-votes))
               (btn-default id text))]]
    )))

(defcomponent question-widget [{:keys [text]} _]
  (render
   [_]
   (html
    [:div
     [:h2 text]])))


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
      [:canvas#qr {:width (str size "px")
                   :height (str size "px")}]
    )))


(defcomponent app-widget [{:keys [title connected qr question answers]} _]
  (render [_]
    (html
      [:div.wrapper
       [:h1 title]
       (om/build question-widget question)
       (if slide?
         [:div
          [:h2 (str "Connected: " connected)]
          (om/build-all stat-widget answers)
          (om/build qr-widget qr)]
         [:div
          (om/build-all answer-widget answers)
          [:h2 (str "ID=" (pr-str client-id))]])]
      )))

(def root (. js/document (getElementById "app")))
(om/root app-widget state {:target root})

(fig/add-message-watch
 :state-server
 (fn [{:keys [msg-name] :as msg}]
   (when (= msg-name :push-state)
     (println "raw:" msg)
     (reset! state (:state msg)))))
