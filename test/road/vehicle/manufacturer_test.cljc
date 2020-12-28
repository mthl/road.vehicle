(ns road.vehicle.manufacturer-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle :as rv]
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
  (testing "region retrieval"
    (are [wmi r] (= r (rvm/region wmi))
      "AXX" "Africa"
      "DXX" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "manufacturer decoding"
    (are [wmi r]
        (= r (::rvm/region (rvm/decode wmi "XXX")))
      "AXX" "Africa"
      "DXX" nil)))

(deftest country-test
  (testing "country retrieval"
    (are [wmi r] (= r (rvm/country wmi))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "manufacturer decoding"
    (are [wmi reg]
        (= reg (::rvm/country (rvm/decode wmi "ZZZ")))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil)))

(deftest manufacturer-id-test
  (are [wmi extra id]
      (= id (::rvm/id (rvm/decode wmi extra)))
    "XXX" "ABC" "XXX"
    "XX9" "ABC" "XX9/ABC"))
