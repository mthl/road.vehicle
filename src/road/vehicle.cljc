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

(s/def ::model-year
  ;; The year of the vehicle model
  (s/int-in 1980 #?(:clj Integer/MAX_VALUE
                    :cljs Number.MAX_SAFE_INTEGER)))

(defn- max-year []
  #?(:clj (.getYear (java.time.LocalDate/now))
     :cljs (.getFullYear (js/Date.))))

(s/fdef model-year
  :args (s/cat :c (set u/vin-chars) :year ::model-year)
  :ret (s/nilable ::model-year))

(let [model-years (zipmap (remove #{\0 \U \Z} u/vin-chars) (range))]
  (defn model-year
    "Decode the year code `c` present in the 10th character of a VIN.

  Since this encoding is ambiguous and represent a collection of year,
  we make a contextual guess by adding a `year` parameter. The
  vehicule model will be in the 30 year period before
  `year` (inclusive)."
    ([c]
     (model-year c (max-year)))
    ([c year]
     (when-let [idx (model-years c)]
       (let [dis (mod year 30)
             base (if (> idx dis) (- year 30) year)]
         (+ (max (- base dis) 1980) idx))))))

(declare decode)

(s/def ::vehicle
  (s/with-gen
    (s/and
     (s/keys :req [::vin ::wmi ::vds ::vis ::manufacturer]
             :opt [::model-year])
     #(= (::vin %)
         (str (::wmi %) (::vds %) (::vis %))))
    #(gen/fmap decode (s/gen ::vin))))

(s/fdef decode-vehicle
  :args (s/cat :v ::vehicle)
  :ret ::vehicle
  :fn (s/and #(= (-> % :ret ::vin) (-> % :args :v ::vin))))

(defn- decode-vehicle
  [v]
  (let [id (get-in v [::manufacturer ::rvm/id])
        decode (rvm/decoder id identity)]
    (decode v)))

(s/def road.vehicle.manufacturer.renault/decode `decode-vehicle)

(s/fdef decode
  :args (s/cat :vin ::vin)
  :ret ::vehicle
  :fn (s/and #(= (-> % :ret ::vin) (-> % :args :vin))))

(defn decode
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [wmi (subs vin 0 3)
        my (model-year (get vin 9))]
    (decode-vehicle
     (cond-> {::vin vin
              ::wmi wmi
              ::vds (subs vin 3 9)
              ::vis (subs vin 9 17)
              ::manufacturer (rvm/decode wmi (subs vin 11 14))}
       my (assoc ::model-year my)))))
