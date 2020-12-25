(ns vehiclj.core-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [vehiclj.core :as sut]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      :iso-3779/wmi
      :iso-3779/vds
      :iso-3779/vis
      :iso-3779/vin
      :vehiclj/vehicle
      :vehiclj.manufacturer/country
      :vehiclj.manufacturer/region)))

(def fn-specs
  `[sut/country
    sut/decode-vin
    sut/region])

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total (count fn-specs)
            :check-passed (count fn-specs)}
           (stest/summarize-results
            (stest/check fn-specs))))))

(def ^:private a (comp gen/generate s/gen))

(deftest region-test
  (testing "region retrieval"
    (are [wmi r] (= r (sut/region wmi))
      "AXX" "Africa"
      "DXX" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "vin decoding"
    (let [vds (a :iso-3779/vds)
          vis (a :iso-3779/vin)]
      (are [vin reg] (= reg (find (sut/decode-vin vin)
                                  :vehiclj.manufacturer/region))
        (str "AXX" vds vis) [:vehiclj.manufacturer/region "Africa"]
        (str "DXX" vds vis) nil))))

(deftest country-test
  (testing "country retrieval"
    (are [wmi r] (= r (sut/country wmi))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "vin decoding"
    (let [vds (a :iso-3779/vds)
          vis (a :iso-3779/vin)]
      (are [vin reg] (= reg (find (sut/decode-vin vin)
                                  :vehiclj.manufacturer/country))
        (str "6AX" vds vis) [:vehiclj.manufacturer/country "Australia"]
        (str "WAX" vds vis) [:vehiclj.manufacturer/country "Germany"]
        (str "B6X" vds vis) nil))))
