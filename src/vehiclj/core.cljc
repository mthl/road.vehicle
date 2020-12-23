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

(def vin-alphas
  "All valid VIN characters.

  This includes every ASCII alphabetic character except 'I', 'O' and
  'Q' which could be confused with '0' and '1'."
  [\A \B \C \D \E \F \G \H \J \K \L \M \N \P \R \S \T \U \V \W \X \Y \Z])

(def vin-nums
  "All valid VIN numbers.

  This includes all single digit numbers starting from 1 and ending
  with 0."
  [\1 \2 \3 \4 \5 \6 \7 \8 \9 \0])

(def vin-chars
  "All valid VIN characters."
  (into vin-alphas vin-nums))

(def ^:private  char-index
  (zipmap vin-chars (range)))

(defn- char-range
  "Returns the [[vin-chars]] from `start` (inclusive) to
  `end` (inclusive)."
  [start end]
  (subvec vin-chars (char-index start) (inc (char-index end))))

(def ^:private upper-join
  (comp str/upper-case str/join))

(defmacro ^:private fixed-length-upper-string
  "Return a specification matching fixed length strings containing `n`
  alpha-numeric characters."
  ;; Use a macro instead of a function to include the actual `n` value
  ;; when invoking `clojure.spec/describe` on the specs.
  [n]
  (let [rgx (re-pattern (format "[A-Z0-9&&[^IOQ]]{%d}" n))]
    `(s/with-gen (s/and string? #(re-matches ~rgx %))
       #(->> (gen/vector (gen/elements vin-chars) ~n)
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

(def ^:private regions
  {[\A \C] "Africa",
   [\J \R] "Asia",
   [\S \Z] "Europe",
   [\1 \5] "North America",
   [\6 \7] "Oceania",
   [\8 \9] "South America"})

(s/def :vehiclj.manufacturer/region
  #{"Africa" "Asia" "Europe" "North America" "Oceania" "South America"})

(s/fdef region
  :args (s/cat :wmi :iso-3779/wmi)
  :ret (s/nilable :vehiclj.manufacturer/region))

(def ^{:arglists '([wmi])} region
  "Find the region name associated with a World Manufacturer
  Identifier (WMI)."
  (let [lookup (reduce-kv (fn [acc [begin end] reg]
                            (into acc
                                  (map #(vector % reg))
                                  (char-range begin end)))
                          {}
                          regions)]
    (comp lookup first)))

(defn decode-vin
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [wmi (subs vin 0 3)
        vds (subs vin 3 9)
        vis (subs vin 9 17)
        region (region wmi)]
    (cond-> {:iso-3779/vin vin
             :iso-3779/wmi wmi
             :iso-3779/vds vds
             :iso-3779/vis vis}
      region (assoc :vehiclj.manufacturer/region region))))

(s/def :vehiclj/vehicle
  (s/with-gen
    (s/and
     (s/keys :req [:iso-3779/vin :iso-3779/wmi :iso-3779/vds :iso-3779/vis]
             :opt [:vehiclj.manufacturer/region])
     #(= (:iso-3779/vin %)
         (str (:iso-3779/wmi %) (:iso-3779/vds %) (:iso-3779/vis %))))
    #(gen/fmap decode-vin (s/gen :iso-3779/vin))))

(s/fdef decode-vin
  :args (s/cat :vin :iso-3779/vin)
  :ret :vehiclj/vehicle
  :fn (s/and #(= (-> % :ret :iso-3779/vin) (-> % :args :vin))))
