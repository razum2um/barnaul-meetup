(ns jsconf.state)

(defonce state
  (atom {:title "Hi JS Meetup"

         :connected 0
         :question {:text "js vs cljs"}
         :answers [{:id 0 :text "js" :votes #{}}
                   {:id 1 :text "cljs" :votes #{}}]

         :qr {:size 500 :ecl "L"}}
        ))

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

(defn apply-vote [client-id vote-id {:keys [id] :as answer}]
  (if (= id vote-id)
    (update-in answer [:votes] #(conj % client-id))
    (update-in answer [:votes] #(disj % client-id))))

(defn update-votes [client-id vote-id votes]
  (mapv (partial apply-vote client-id vote-id) votes))

(defn update-answers [client-id v]
  (println "update-answers" client-id "-" v)
  (swap! state update-in [:answers] #(update-votes client-id v %)))
