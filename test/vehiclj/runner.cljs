(ns vehiclj.runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [clojure.test.check]
   [clojure.test.check.properties]
   [vehiclj.core-test]
   [vehiclj.fr-test]))

(doo-tests 'vehiclj.core-test
           'vehiclj.fr-test)
