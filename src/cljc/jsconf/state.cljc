(ns jsconf.state)

(defonce state
  (atom {:title "Hi JS Meetup"
         :connected 0
         :qr {:size 500 :ecl "L"}
         }))

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
