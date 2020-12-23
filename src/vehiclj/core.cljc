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

(def ^:private char-index
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

(s/def ::range
  (s/and (s/coll-of (set vin-chars) :count 2)
         #(<= (-> % first char-index)
              (-> % second char-index))))

(s/def ::range-map
  (s/and (s/map-of ::range string?)
         #(let [cs (mapcat (fn [[x y]]
                             (if (= x y) [x] [x y]))
                           (keys %))]
            (and (apply distinct? nil cs)
                 (= cs (sort-by char-index cs))))))

(s/fdef compile-ranges
  :args (s/cat :rmap ::range-map)
  :ret (s/map-of (set vin-chars) string?)
  :fn (s/and #(= (-> % :args :rmap vals set)
                 (-> % :ret vals set))
             #(every? (-> % :ret keys set)
                      (-> % :args :rmap keys flatten))))

(defn- compile-ranges
  "Expand char ranges map."
  [m]
  (reduce-kv (fn [acc [begin end] label]
               (into acc
                     (map #(vector % label))
                     (char-range begin end)))
             {}
             m))

(s/def :vehiclj.manufacturer/region
  #{"Africa" "Asia" "Europe" "North America" "Oceania" "South America"})

(s/fdef region
  :args (s/cat :wmi :iso-3779/wmi)
  :ret (s/nilable :vehiclj.manufacturer/region))

(def ^{:arglists '([wmi])} region
  "Find the region name associated with a World Manufacturer
  Identifier (WMI)."
  (let [lookup (compile-ranges regions)]
    (comp lookup first)))

(def ^:private countries
  "Map from WMI first letter to a map of country to set of WMI second
  letters."
  {\A {[\A \H] "South Africa"
       [\K \J] "Ivory Coast"
       [\L \M] "Lesotho"
       [\N \P] "Botswana"
       [\R \S] "Namibia"
       [\T \U] "Madagascar"
       [\V \W] "Mauritius"
       [\X \Y] "Tunisia"
       [\4 \5] "Mozambique"
       [\Z \1] "Cyprus"
       [\2 \3] "Zimbabwe"}
   \B {[\A \B] "Angola"
       [\F \G] "Kenya"
       [\L \L] "Nigeria"
       [\R \R] "Algeria"
       [\3 \4] "Libya"}
   \C {[\A \B] "Egypt"
       [\F \G] "Morocco"
       [\L \M] "Zambia"}
   \J {[\A \0] "Japan"}
   \K {[\A \E] "Sri Lanka"
       [\F \K] "Israel"
       [\L \R] "S Korea"
       [\S \0] "Kazakhstan"}
   \L {[\A \0] "China"}
   \M {[\A \E] "India"
       [\F \K] "Indonesia"
       [\L \R] "Thailand"
       [\S \S] "Myanmar"}
   \N {[\A \E] "Iran"
       [\F \K] "Pakistan"
       [\L \R] "Turkey"}
   \P {[\A \E] "Philippines"
       [\F \K] "Singapore"
       [\L \R] "Malaysia"}
   \R {[\A \E] "UAE"
       [\F \K] "Taiwan"
       [\L \R] "Vietnam"
       [\S \0] "Saudi Arabia"}
   \S {[\A \M] "United Kingdom"
       [\N \T] "E Germany"
       [\U \Z] "Poland"
       [\1 \4] "Latvia"}
   \T {[\A \H] "Switzerland"
       [\J \P] "Czech Rep"
       [\R \V] "Hungary"
       [\W \1] "Portugal"}
   \U {[\H \M] "Denmark"
       [\N \T] "Ireland"
       [\U \Z] "Romania"
       [\5 \7] "Slovak."}
   \V {[\A \E] "Austria"
       [\F \R] "France"
       [\S \W] "Spain"
       [\X \2] "Serbia"
       [\3 \5] "Croatia"
       [\6 \0] "Estonia"}
   \W {[\A \0] "Germany"}
   \X {[\A \E] "Bulgaria"
       [\F \K] "Greece"
       [\L \K] "Netherlands"
       [\S \W] "USSR"
       [\X \2] "Luxembourg"
       [\3 \0] "Russia"}
   \Y {[\A \E] "Belgium"
       [\F \K] "Finland"
       [\L \R] "Malta"
       [\S \W] "Sweden"
       [\X \2] "Norway"
       [\3 \5] "Belarus"
       [\6 \0] "Ukraine"}
   \Z {[\A \R] "Italy"
       [\X \2] "Slovenia"
       [\3 \5] "Lithuania"}
   \1 {[\A \0] "United States"}
   \2 {[\A \0] "Canada"}
   \3 {[\A \0] "Mexico"}
   \4 {[\A \0] "United States"}
   \5 {[\A \0] "United States"}
   \6 {[\A \W] "Australia"}
   \7 {[\A \E] "New Zealand"}
   \8 {[\A \E] "Argentina"
       [\F \K] "Chile"
       [\L \R] "Ecuador"
       [\S \W] "Peru"
       [\X \2] "Venezuela"}
   \9 {[\A \E] "Brazil"
       [\F \K] "Colombia"
       [\L \R] "Paraguay"
       [\S \V] "Uruguay"
       [\X \2] "Trinidad & Tobago"
       [\3 \9] "Brazil"}})

(s/def :vehiclj.manufacturer/country
  #{"Algeria"
    "Angola"
    "Argentina"
    "Australia"
    "Austria"
    "Belarus"
    "Belgium"
    "Botswana"
    "Brazil"
    "Bulgaria"
    "Canada"
    "Chile"
    "China"
    "Colombia"
    "Croatia"
    "Cyprus"
    "Czech Rep"
    "Denmark"
    "E Germany"
    "Ecuador"
    "Egypt"
    "Estonia"
    "Finland"
    "France"
    "Germany"
    "Greece"
    "Hungary"
    "India"
    "Indonesia"
    "Iran"
    "Ireland"
    "Israel"
    "Italy"
    "Ivory Coast"
    "Japan"
    "Kazakhstan"
    "Kenya"
    "Latvia"
    "Lesotho"
    "Libya"
    "Lithuania"
    "Luxembourg"
    "Madagascar"
    "Malaysia"
    "Malta"
    "Mauritius"
    "Mexico"
    "Morocco"
    "Mozambique"
    "Myanmar"
    "Namibia"
    "Netherlands"
    "New Zealand"
    "Nigeria"
    "Norway"
    "Pakistan"
    "Paraguay"
    "Peru"
    "Philippines"
    "Poland"
    "Portugal"
    "Romania"
    "Russia"
    "S Korea"
    "Saudi Arabia"
    "Serbia"
    "Singapore"
    "Slovak."
    "Slovenia"
    "South Africa"
    "Spain"
    "Sri Lanka"
    "Sweden"
    "Switzerland"
    "Taiwan"
    "Thailand"
    "Trinidad & Tobago"
    "Tunisia"
    "Turkey"
    "UAE"
    "USSR"
    "Ukraine"
    "United Kingdom"
    "United States"
    "Uruguay"
    "Venezuela"
    "Vietnam"
    "Zambia"
    "Zimbabwe"})

(s/fdef country
  :args (s/cat :wmi :iso-3779/wmi)
  :ret (s/nilable :vehiclj.manufacturer/country))

(def ^{:arglists '([wmi])} country
  "Find the country name associated with a World Manufacturer
  Identifier (WMI)."
  (let [lookup (zipmap (keys countries) (map compile-ranges (vals countries)))]
    (fn [wmi]
      (-> lookup
          (get (first wmi))
          (get (second wmi))))))

(defn decode-vin
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [wmi (subs vin 0 3)
        vds (subs vin 3 9)
        vis (subs vin 9 17)
        region (region wmi)
        country (country wmi)]
    (cond-> {:iso-3779/vin vin
             :iso-3779/wmi wmi
             :iso-3779/vds vds
             :iso-3779/vis vis}
      region (assoc :vehiclj.manufacturer/region region)
      country (assoc :vehiclj.manufacturer/country country))))

(s/def :vehiclj/vehicle
  (s/with-gen
    (s/and
     (s/keys :req [:iso-3779/vin :iso-3779/wmi :iso-3779/vds :iso-3779/vis]
             :opt [:vehiclj.manufacturer/region
                   :vehiclj.manufacturer/country])
     #(= (:iso-3779/vin %)
         (str (:iso-3779/wmi %) (:iso-3779/vds %) (:iso-3779/vis %))))
    #(gen/fmap decode-vin (s/gen :iso-3779/vin))))

(s/fdef decode-vin
  :args (s/cat :vin :iso-3779/vin)
  :ret :vehiclj/vehicle
  :fn (s/and #(= (-> % :ret :iso-3779/vin) (-> % :args :vin))))
