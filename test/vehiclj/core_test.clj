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
      :vehiclj/manufacturer
      :vehiclj/vehicle
      :vehiclj.manufacturer/country
      :vehiclj.manufacturer/id
      :vehiclj.manufacturer/region)))

(def fn-specs
  `[sut/country
    sut/decode-manufacturer
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

  (testing "manufacturer decoding"
    (are [wmi r]
        (= r (:vehiclj.manufacturer/region
              (sut/decode-manufacturer #:iso-3779{:wmi wmi})))
      "AXX" "Africa"
      "DXX" nil)))

(deftest country-test
  (testing "country retrieval"
    (are [wmi r] (= r (sut/country wmi))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "manufacturer decoding"
    (are [wmi reg]
        (= reg (:vehiclj.manufacturer/country
                (sut/decode-manufacturer {:iso-3779/wmi wmi})))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil)))

(deftest manufacturer-id-test
  (are [vin id]
      (= id (:vehiclj.manufacturer/id
             (sut/decode-manufacturer
              #:iso-3779{:vin vin :wmi (subs vin 0 3)})))
    "XXXYYYYYYZZZZZZZZ" "XXX"
    "XX9YYYYYYZZABCZZZ" "XX9/ABC"))
