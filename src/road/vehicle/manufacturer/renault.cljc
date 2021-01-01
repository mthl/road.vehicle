(ns road.vehicle.manufacturer.renault
  "Decoding rules specific to Renault manufacturer.")

(def ^:private body-types
  {\B ::#5-doors
   \C ::#3-doors
   \D ::coupe
   \K ::break})

(def ^:private engine-types
  {\2 "petrol engine with regular injection"
   \7 "petrol engine with fuel injection"
   \G "Gazole engine"})

(def ^:private transmissions
  {\5 ::#5-gears})

(def ^:private plants
  (let [m {\K {"United States" "Kenosha, Wisconsin"
               "France" "Dieppe"}
           \R {"France" "Romorantin"}}]
    (fn [c country]
      (get-in m [c country]))))

(defn decode
  [{:road.vehicle/keys [vds vis manufacturer] :as v}]
  (let [{:road.vehicle.manufacturer/keys [country]} manufacturer
        body-type (-> vds first body-types)
        engine-type (-> vds second engine-types)
        transmission (-> vds (nth 4) transmissions)
        plant (-> vis second (plants country))
        serial (subs vis 2)]
    (cond-> (assoc v :road.vehicle/serial-number serial)
      body-type (assoc :road.vehicle/body-type body-type)
      engine-type (assoc :road.vehicle/engine-type engine-type)
      transmission (assoc :road.vehicle/transmission transmission)
      plant (assoc :road.vehicle/plant plant))))
