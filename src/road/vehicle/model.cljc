(ns road.vehicle.model
  "Identification of vehicle models.

  A vehicle model is a commercial identification used by manufacturers
  to refer to a collection a similar vehicles."
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::year
  ;; The year of the vehicle model
  (s/int-in 1980 #?(:clj Integer/MAX_VALUE
                    :cljs Number.MAX_SAFE_INTEGER)))

(s/def :road.vehicle/model
  (s/keys :opt [::year]))
