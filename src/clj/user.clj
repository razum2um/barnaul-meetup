(ns user
  (:require [reloaded.repl :refer [system init start stop go reset set-init!]]
            [cljs.repl.node]
            [cljs.repl]
            [aprint.core]
            [environ.core :refer [env]]
            [jsconf.core]))

(set-init! #'jsconf.core/dev-system)

(aprint.utils/use-method aprint.dispatch/color-dispatch :figwheel-sidecar.utils/compiler-env pr)
(def p aprint.core/aprint)

(defn node-repl []
  (cljs.repl/repl (cljs.repl.node/repl-env)))
