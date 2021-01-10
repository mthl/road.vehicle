(ns road.vehicle.fr-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer [are deftest is testing]]
   [road.vehicle.fr :as fr]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::fr/carrosserie
      ::fr/cnit
      ::fr/genre)))

(deftest genres-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::fr/genre)
           (-> fr/genres keys set))))

  (testing "genres description type"
    (is (s/valid? (-> `fr/genres s/spec :ret s/coll-of)
                  (vals fr/genres)))))

(deftest carrosseries-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::fr/carrosserie)
           (-> fr/carrosseries keys set))))

  (testing "carrosseries description type"
    (is (s/valid? (-> `fr/carrosseries s/spec :ret s/coll-of)
                  (vals fr/carrosseries)))))
