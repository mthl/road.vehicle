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
      :iso-3779/vin)))

(def fn-specs
  `[sut/decode-vin])

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total (count fn-specs)
            :check-passed (count fn-specs)}
           (stest/summarize-results
            (stest/check fn-specs))))))
