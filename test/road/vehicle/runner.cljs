(ns road.vehicle.runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [clojure.test.check]
   [clojure.test.check.properties]
   [road.vehicle-test]
   [road.vehicle.fr-test]
   [road.vehicle.manufacturer-test]))

(doo-tests 'road.vehicle-test
           'road.vehicle.fr-test
           'road.vehicle.manufacturer-test)
