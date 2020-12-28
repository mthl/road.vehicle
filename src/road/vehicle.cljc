(ns road.vehicle
  "International vehicle identification properties.

  ISO-3779 defines the notion of global Vehicle Identification
  Number (VIN) and the semantics of its WMI, VDS, VIS sections (See
  <https://en.wikipedia.org/wiki/Vehicle_identification_number> for
  more details)."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as str]
   [road.vehicle.manufacturer :as rvm]
   [road.vehicle.util :as u]))

(s/def ::vin
  ;; Global unique Vehicle Identification Number (VIN).
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{17}" %))
    #(u/vin-str-gen 17)))

(s/def ::wmi
  ;; The World Manufacturer Identifier (WMI) attributed by the Society of
  ;; Automotive Engineers (SAE).
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{3}" %))
    #(u/vin-str-gen 3)))

(s/def ::vds
  ;; The Vehicle Descriptor Section of the VIN identifying the vehicle
  ;; type according to local regulations.
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{6}" %))
    #(u/vin-str-gen 6)))

(s/def ::vis
  ;; The Vehicle Identifier Section of the VIN used by the
  ;; manufacturer to identify each individual vehicle.
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{8}" %))
    #(u/vin-str-gen 8)))

(declare decode)

(s/def ::vehicle
  (s/with-gen
    (s/and
     (s/keys :req [::vin ::wmi ::vds ::vis ::manufacturer])
     #(= (::vin %)
         (str (::wmi %) (::vds %) (::vis %))))
    #(gen/fmap decode (s/gen ::vin))))

(s/fdef decode
  :args (s/cat :vin ::vin)
  :ret ::vehicle
  :fn (s/and #(= (-> % :ret ::vin) (-> % :args :vin))))

(defn decode
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [wmi (subs vin 0 3)]
    {::vin vin
     ::wmi wmi
     ::vds (subs vin 3 9)
     ::vis (subs vin 9 17)
     ::manufacturer (rvm/decode wmi (subs vin 11 14))}))
