(ns road.vehicle-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle :as vehicle]
   [road.vehicle.manufacturer :as manufacturer]
   [road.vehicle.model :as model]
   [road.vehicle.manufacturer.renault :as renault]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::vehicle/wmi
      ::vehicle/vds
      ::vehicle/vis
      ::vehicle/vin
      ::vehicle/manufacturer
      ::vehicle/vehicle)))

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total 2
            :check-passed 2}
           (stest/summarize-results
            (stest/check `[vehicle/decode
                           vehicle/model-year]))))))

(deftest model-year
  (are [c y r] (= r (vehicle/model-year c y))
    \C 2020 2012
    \C 2012 2012
    \D 2012 1983
    \D 2020 2013
    \K 1970 1989
    \K 1880 1989))

(deftest vin-examples
  (testing "Mazda 6"
    (is (= #::vehicle{:vin "JMZGJ627661337940"
                      :wmi "JMZ"
                      :vds "GJ6276"
                      :vis "61337940"
                      :model #::model{:year 2006}
                      :manufacturer #::manufacturer{:id "JMZ"
                                                    :name "Mazda"
                                                    :country "Japan"
                                                    :region "Asia"}}
           (vehicle/decode "JMZGJ627661337940"))))

  (testing "Renault Laguna"
    (is (= #::vehicle{:vin "VF1KG1P5E3R488860"
                      :wmi "VF1"
                      :vds "KG1P5E"
                      :vis "3R488860"
                      :model #::model{:year 2003}
                      :manufacturer #::manufacturer{:id "VF1"
                                                    :region "Europe"
                                                    :country "France"
                                                    :name "Renault"}
                      :plant "Romorantin"
                      :engine-type "Gazole engine"
                      :body-type ::renault/break
                      :transmission ::renault/#5-gears
                      :serial-number "488860"}
           (vehicle/decode "VF1KG1P5E3R488860")))))
