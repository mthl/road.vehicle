(ns road.vehicle-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle :as rv]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::rv/wmi
      ::rv/vds
      ::rv/vis
      ::rv/vin
      ::rv/manufacturer
      ::rv/vehicle
      :road.vehicle.manufacturer/country
      :road.vehicle.manufacturer/id
      :road.vehicle.manufacturer/name
      :road.vehicle.manufacturer/region)))

(deftest check-fns
  (testing "function specs conformance"
    (is (= {:total 5
            :check-passed 5}
           (stest/summarize-results
            (stest/check `[rv/country
                           rv/decode-manufacturer
                           rv/decode-vin
                           rv/manufacturer
                           rv/region]))))))

(def ^:private a (comp gen/generate s/gen))

(deftest region-test
  (testing "region retrieval"
    (are [wmi r] (= r (rv/region wmi))
      "AXX" "Africa"
      "DXX" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "manufacturer decoding"
    (are [wmi r]
        (= r (:road.vehicle.manufacturer/region
              (rv/decode-manufacturer #::rv{:wmi wmi})))
      "AXX" "Africa"
      "DXX" nil)))

(deftest country-test
  (testing "country retrieval"
    (are [wmi r] (= r (rv/country wmi))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil
      "OXX" nil
      "IXX" nil
      "QXX" nil))

  (testing "manufacturer decoding"
    (are [wmi reg]
        (= reg (:road.vehicle.manufacturer/country
                (rv/decode-manufacturer {::rv/wmi wmi})))
      "6AX" "Australia"
      "WAX" "Germany"
      "B6X" nil)))

(deftest manufacturer-id-test
  (are [vin id]
      (= id (:road.vehicle.manufacturer/id
             (rv/decode-manufacturer
              #::rv{:vin vin :wmi (subs vin 0 3)})))
    "XXXYYYYYYZZZZZZZZ" "XXX"
    "XX9YYYYYYZZABCZZZ" "XX9/ABC"))

(deftest mazda-6-test
  (is (= #::rv{:vin "JMZGJ627661337940"
               :wmi "JMZ"
               :vds "GJ6276"
               :vis "61337940"
               :manufacturer
               #:road.vehicle.manufacturer{:id "JMZ"
                                           :name "Mazda"
                                           :country "Japan"
                                           :region "Asia"}}
         (rv/decode-vin "JMZGJ627661337940"))))
