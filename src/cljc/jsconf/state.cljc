(ns jsconf.state)

(def state
  (atom {:title "Привет JS Meetup"
         :question {:text "JS FTW?"}
         :answers [{:id 0 :text "Да" :votes #{}}
                   {:id 1 :text "Нет" :votes #{}}]
         :qr {:size 500
              :ecl "L"
              :value "http://razum2um.me"}}))

(defn- log [message atom]
  (->> @atom
       pr-str
       (println message)))

(defn update-state [& args]
  (let [value (last args)
        path (->> args butlast (map keyword))]
    (when value
      (log "prev:" state)
      (swap! state #(assoc-in % path value)))
    (log "current:" state)))
