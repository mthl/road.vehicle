(ns vehiclj.fr-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer [are deftest is testing]]
   [vehiclj.fr :as sut]))

(deftest data-spec-generators
  (testing "generation of values conforming to data specs."
    (are [spec] (= 10 (count (gen/sample (s/gen spec) 10)))
      ::sut/carrosserie
      ::sut/cnit
      ::sut/genre)))

(deftest genres-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::sut/genre)
           (-> sut/genres keys set))))

  (testing "genres description type"
    (is (s/valid? (-> `sut/genres s/spec :ret s/coll-of)
                  (vals sut/genres)))))

(deftest carrosseries-test
  (testing "consistency and completeness of genres specification"
    (is (= (s/form ::sut/carrosserie)
           (-> sut/carrosseries keys set))))

  (testing "carrosseries description type"
    (is (s/valid? (-> `sut/carrosseries s/spec :ret s/coll-of)
                  (vals sut/carrosseries)))))
