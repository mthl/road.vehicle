(ns road.vehicle.manufacturer-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle.manufacturer :as rvm]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::rvm/country
      ::rvm/id
      ::rvm/name
      ::rvm/region)))

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total 4
            :check-passed 4}
           (stest/summarize-results
            (stest/check `[rvm/country
                           rvm/decode
                           rvm/name
                           rvm/region]))))))

(deftest region-test
  (are [wmi r] (= r (rvm/region wmi))
    "AXX" "Africa"
    "DXX" nil
    "OXX" nil
    "IXX" nil
    "QXX" nil))

(deftest country-test
  (are [wmi c] (= c (rvm/country wmi))
    "6AX" "Australia"
    "WAX" "Germany"
    "B6X" nil
    "OXX" nil
    "IXX" nil
    "QXX" nil))

(deftest name-tesg
  (are [id n] (= n (rvm/name id))
    "VF1" "Renault"
    "JMZ" "Mazda"
    "VW3" nil
    "IOQ" nil))

(deftest manufacturer-id-test
  (are [wmi extra v] (= v (rvm/decode wmi extra))
    "XXX" "ABC" #::rvm{:id "XXX" :region "Europe" :country "Luxembourg"}
    "XX9" "ABC" #::rvm{:id "XX9/ABC" :region "Europe" :country "Luxembourg"}
    "6AX" "ZZZ" #::rvm{:id "6AX" :region "Oceania" :country "Australia"}
    "WAX" "ZZZ" #::rvm{:id "WAX" :region "Europe" :country "Germany"}
    "B6X" "ZZZ" #::rvm{:id "B6X" :region "Africa"}
    "AXX" "XXX" #::rvm{:id "AXX" :region "Africa" :country "Tunisia"}
    "DXX" "XXX" #::rvm{:id "DXX"}
    "WDB" "ABC" #::rvm{:id "WDB"
                       :name "Mercedes-Benz"
                       :region "Europe"
                       :country "Germany"}))
