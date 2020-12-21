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
      :vehiclj/region
      :vehiclj/vehicle)))

(def fn-specs
  `[sut/decode-vin
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
    (let [vds (a :iso-3779/vds)
          vis (a :iso-3779/vin)]
      (are [wmi r] (= r (sut/region (str wmi vds vis)))
        "AXX" "Africa"
        "DXX" nil)))

  (testing "vin decording"
    (let [vds (a :iso-3779/vds)
          vis (a :iso-3779/vin)]
      (are [vin reg] (= reg (find (sut/decode-vin vin) :vehiclj/region))
        (str "AXX" vds vis) [:vehiclj/region "Africa"]
        (str "DXX" vds vis) nil))))
