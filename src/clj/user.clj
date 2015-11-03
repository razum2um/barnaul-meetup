(ns user
  (:require [reloaded.repl :refer [system init start stop go reset set-init!]]
            [aprint.core]
            [jsconf.core]))

(set-init! #'jsconf.core/dev-system)
(def p aprint.core/aprint)
