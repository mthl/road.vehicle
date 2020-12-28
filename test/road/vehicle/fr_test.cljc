(ns road.vehicle.fr-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle.fr :as rvf]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::rvf/carrosserie
      ::rvf/cnit
      ::rvf/genre)))

(deftest genres-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::rvf/genre)
           (-> rvf/genres keys set))))

  (testing "genres description type"
    (is (s/valid? (-> `rvf/genres s/spec :ret s/coll-of)
                  (vals rvf/genres)))))

(deftest carrosseries-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::rvf/carrosserie)
           (-> rvf/carrosseries keys set))))

  (testing "carrosseries description type"
    (is (s/valid? (-> `rvf/carrosseries s/spec :ret s/coll-of)
                  (vals rvf/carrosseries)))))
