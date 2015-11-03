(defproject jsconf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [ring "1.4.0"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [org.omcljs/om "0.8.8"]
                 [sablono "0.3.6"]
                 [aprint "0.1.3"]
                 [reloaded.repl "0.2.1"]
                 [environ "1.0.1"]
                 [leiningen "2.5.3"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :main ^:skip-aot jsconf.core}}
  :checkout-deps-shares ^:replace [:source-paths :resource-paths :compile-path]

  :plugins [[lein-figwheel "0.5.0-SNAPSHOT"]
            [lein-environ "1.0.1"]]
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  )
