(ns vehiclj.fr.siv-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer [deftest testing is]]
   [vehiclj.fr.siv :as sut]))

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
