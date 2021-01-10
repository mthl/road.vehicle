(ns road.vehicle.manufacturer-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle.manufacturer :as manufacturer]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::manufacturer/country
      ::manufacturer/id
      ::manufacturer/name
      ::manufacturer/region)))

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total 4
            :check-passed 4}
           (stest/summarize-results
            (stest/check `[manufacturer/country
                           manufacturer/decode
                           manufacturer/name
                           manufacturer/region]))))))

(deftest region-test
  (are [wmi r] (= r (manufacturer/region wmi))
    "AXX" "Africa"
    "DXX" nil
    "OXX" nil
    "IXX" nil
    "QXX" nil))

(deftest country-test
  (are [wmi c] (= c (manufacturer/country wmi))
    "6AX" "Australia"
    "WAX" "Germany"
    "B6X" nil
    "OXX" nil
    "IXX" nil
    "QXX" nil))

(deftest name-test
  (are [id n] (= n (manufacturer/name id))
    "VF1" "Renault"
    "JMZ" "Mazda"
    "VW3" nil
    "IOQ" nil))

(deftest manufacturer-id-test
  (are [wmi extra v] (= v (manufacturer/decode wmi extra))
    "XXX" "ABC" #::manufacturer{:id "XXX"
                                :region "Europe"
                                :country "Luxembourg"}
    "XX9" "ABC" #::manufacturer{:id "XX9/ABC"
                                :region "Europe"
                                :country "Luxembourg"}
    "6AX" "ZZZ" #::manufacturer{:id "6AX"
                                :region "Oceania"
                                :country "Australia"}
    "WAX" "ZZZ" #::manufacturer{:id "WAX" :region "Europe" :country "Germany"}
    "B6X" "ZZZ" #::manufacturer{:id "B6X" :region "Africa"}
    "AXX" "XXX" #::manufacturer{:id "AXX" :region "Africa" :country "Tunisia"}
    "DXX" "XXX" #::manufacturer{:id "DXX"}
    "WDB" "ABC" #::manufacturer{:id "WDB"
                                :name "Mercedes-Benz"
                                :region "Europe"
                                :country "Germany"}))
