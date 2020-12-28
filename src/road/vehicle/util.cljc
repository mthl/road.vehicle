(ns road.vehicle.util
  "Internal utilitary namespace not part of road.vehicle public API"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as str]))

(def ^:private vin-alphas
  "All valid VIN characters.

  This includes every ASCII alphabetic character except 'I', 'O' and
  'Q' which could be confused with '0' and '1'."
  [\A \B \C \D \E \F \G \H \J \K \L \M \N \P \R \S \T \U \V \W \X \Y \Z])

(def ^:private vin-nums
  "All valid VIN numbers.

  This includes all single digit numbers starting from 1 and ending
  with 0."
  [\1 \2 \3 \4 \5 \6 \7 \8 \9 \0])

(def vin-chars
  "All valid VIN characters."
  (into vin-alphas vin-nums))

(def ^:private char-index
  (zipmap vin-chars (range)))

(defn char-range
  "Returns the [[u/vin-chars]] from `start` (inclusive) to
  `end` (inclusive)."
  [start end]
  (subvec vin-chars (char-index start) (inc (char-index end))))

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

(defn compile-ranges
  "Expand char ranges map."
  [m]
  (reduce-kv (fn [acc [begin end] label]
               (into acc
                     (map #(vector % label))
                     (char-range begin end)))
             {}
             m))

(def ^:private upper-join
  (comp str/upper-case str/join))

(defn vin-str-gen
  [n]
  (->> n
       (gen/vector (gen/elements vin-chars))
       (gen/fmap upper-join)))
