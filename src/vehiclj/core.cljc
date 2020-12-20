(ns vehiclj.core
  "International vehicle identification properties.

  ISO-3779 defines the notion of global Vehicle Identification
  Number (VIN) and the semantics of its WMI, VDS, VIS sections (See
  <https://en.wikipedia.org/wiki/Vehicle_identification_number> for
  more details)."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as str]))

(def ^:private upper-join
  (comp str/upper-case str/join))

(defmacro ^:private fixed-length-upper-string
  "Return a specification matching fixed length strings containing `n`
  alpha-numeric characters."
  ;; Use a macro instead of a function to include the actual `n` value
  ;; when invoking `clojure.spec/describe` on the specs.
  [n]
  (let [rgx (re-pattern (format "[A-Z0-9]{%d}" n))]
    `(s/with-gen (s/and string? #(re-matches ~rgx %))
       #(->> (gen/vector (gen/char-alphanumeric) ~n)
             (gen/fmap upper-join)))))

(s/def :iso-3779/vin
  ;; Global unique Vehicle Identification Number (VIN).
  (fixed-length-upper-string 17))

(s/def :iso-3779/wmi
  ;; The World Manufacturer Identifier (WMI) attributed by the Society of
  ;; Automotive Engineers (SAE).
  (fixed-length-upper-string 3))

(s/def :iso-3779/vds
  ;; The Vehicle Descriptor Section of the VIN identifying the vehicle
  ;; type according to local regulations.
  (fixed-length-upper-string 6))

(s/def :iso-3779/vis
  ;; The Vehicle Identifier Section of the VIN used by the
  ;; manufacturer to identify each individual vehicle.
  (fixed-length-upper-string 8))

(s/def :vehiclj/vehicle
  (s/and
   (s/keys :req [:iso-3779/vin :iso-3779/wmi :iso-3779/vds :iso-3779/vis])
   #(= (:iso-3779/vin %)
       (str (:iso-3779/wmi %) (:iso-3779/vds %) (:iso-3779/vis %)))))

(s/fdef decode-vin
  :args (s/cat :vin :iso-3779/vin)
  :ret :vehiclj/vehicle
  :fn (s/and #(= (-> % :ret :iso-3779/vin) (-> % :args :vin))))

(defn decode-vin
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [wmi (subs vin 0 3)
        vds (subs vin 3 9)
        vis (subs vin 9 17)]
    {:iso-3779/vin vin
     :iso-3779/wmi wmi
     :iso-3779/vds vds
     :iso-3779/vis vis}))
