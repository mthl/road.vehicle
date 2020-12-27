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

(s/def :iso-3779/vin
  ;; Global unique Vehicle Identification Number (VIN).
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{17}" %))
    #(->> (gen/vector (gen/elements vin-chars) 17)
          (gen/fmap upper-join))))

(s/def :iso-3779/wmi
  ;; The World Manufacturer Identifier (WMI) attributed by the Society of
  ;; Automotive Engineers (SAE).
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{3}" %))
    #(->> (gen/vector (gen/elements vin-chars) 3)
          (gen/fmap upper-join))))

(s/def :iso-3779/vds
  ;; The Vehicle Descriptor Section of the VIN identifying the vehicle
  ;; type according to local regulations.
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{6}" %))
    #(->> (gen/vector (gen/elements vin-chars) 6)
          (gen/fmap upper-join))))

(s/def :iso-3779/vis
  ;; The Vehicle Identifier Section of the VIN used by the
  ;; manufacturer to identify each individual vehicle.
  (s/with-gen (s/and string? #(re-matches #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{8}" %))
    #(->> (gen/vector (gen/elements vin-chars) 8)
          (gen/fmap upper-join))))

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

(s/def :vehiclj/small-manufacturer-id
  (s/with-gen
    (s/and string?
           #(re-matches
             #"[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{3}/[ABCDEFGHJKLMNPRSTUVWXYZ0-9]{3}"
             %))
    #(->> (gen/vector (gen/elements vin-chars) 7)
          (gen/fmap (fn [cs] (upper-join (assoc cs 3 \/)))))))

(s/def :vehiclj.manufacturer/id
  (s/or :big :iso-3779/wmi
        :small :vehiclj/small-manufacturer-id))

(s/fdef ids
  :args (s/cat :prefix string? :chars (s/? ::range))
  :ret (s/coll-of string?))

(defn- ids
  "Construct a collection of manufacturer ids prefixed by `prefix` and
  concatenated with a range of vin characters from `start` to `end`."
  ([prefix]
   (ids prefix [\A \0]))
  ([prefix [start end]]
   (map (partial str prefix)
        (char-range start end))))

(s/def manufacturers
  (s/map-of string? (s/coll-of :vehiclj.manufacturer/id)))

(def ^:private manufacturers
  "Mapping from manufacturer name to id"
  {"Volkswagen South Africa" ["AAV"]
   "Hyundai South Africa" ["AC5" "ADD"]
   "Ford South Africa" ["AFA"]
   "Toyota South Africa" ["AHT"]
   "Mitsubishi" ["JA3" "JA4"]
   "Isuzu" (remove #{"JA3" "JA4"} (ids "JA"))
   "Daihatsu" (ids "JD")
   "Fuji Heavy Industries (Subaru)" (ids "JF")
   "Hino" (ids "JH" [\A \E])
   "Honda" (ids "JH" )
   "Kawasaki (motorcycles)" (ids "JK")
   "Mitsubishi Fuso" ["JL5"]
   "Mazda" ["JM1" "JMZ"]
   "Mitsubishi Motors" ["JMB" "JMY"]
   "Nissan" (ids "JN")
   "Suzuki" (ids "JS")
   "Toyota" (into (ids "JT") (ids "4T"))
   "Yamaha (motorcycles)" (ids "JY")
   "Daewoo General Motors South Korea" (ids "KL")
   "Hyundai" (remove #{"KMY" "KM1"} (ids "KM"))
   "Daelim (motorcycles)" ["KMY"]
   "Hyosung (motorcycles)" ["KM1"]
   "Kia" (remove #{"KNM"} (ids "KN"))
   "Renault Samsung" ["KNM"]
   "SsangYong" ["KPA" "KPT"]
   "Jinan Qingqi Motorcycle" ["LAE"]
   "Sundiro Honda Motorcycle" ["LAL"]
   "Changzhou Yamasaki Motorcycle" ["LAN"]
   "Zhejiang Qianjiang Motorcycle (Keeway/Generic)" ["LBB"]
   "Beijing Hyundai" ["LBE"]
   "Zongshen Piaggio" ["LBM"]
   "Chongqing Jainshe Yamaha (motorcycles)" ["LBP"]
   "Geely Motorcycles" ["LB2"]
   "Hangzhou Chunfeng Motorcycles (CFMOTO)" ["LCE"]
   "Dong Feng Peugeot Citroen (DPCA), China" ["LDC"]
   "Dandong Huanghai Automobile" ["LDD"]
   "Dezhou Fulu Vehicle (motorcycles)" ["LDF"]
   "SouEast Motor" ["LDN"]
   "Zhongtong Coach, China" ["LDY"]
   "Jiangling-Isuzu Motors, China" ["LET"]
   "Beijing Benz, China" ["LE4"]
   "FAW, China (busses)" ["LFB"]
   "Taizhou Chuanl Motorcycle Manufacturing" ["LFG"]
   "FAW, China (passenger vehicles)" ["LFP"]
   "FAW, China (trailers)" ["LFT"]
   "FAW-Volkswagen, China" ["LFV"]
   "FAW JieFang, China" ["LFW"]
   "Changshu Light Motorcycle Factory" ["LFY"]
   "Dong Feng (DFM), China" ["LGB"]
   "Qoros (formerly Dong Feng (DFM)), China" ["LGH"]
   "BYD Auto, China" ["LGX"]
   "Beijing Automotive Industry Holding" ["LHB"]
   "FAW-Haima, China" ["LH1"]
   "JAC, China" ["LJC" "LJ1"]
   "Suzhou King Long, China" ["LKL"]
   "Hunan Changfeng Manufacture Joint-Stock" ["LL6"]
   "Linhai (ATV)" ["LL8"]
   "Suzuki Hong Kong (motorcycles)" ["LMC"]
   "Yamaha Hong Kong (motorcycles)" ["LPR"]
   "Shanghai General Motors, China" ["LSG"]
   "MG Motor UK Limited - SAIC Motor, Shanghai, China" ["LSJ"]
   "Shanghai Volkswagen, China" ["LSV"]
   "Brilliance Zhonghua" ["LSY"]
   "National Electric Vehicle Sweden AB (NEVS)" ["LTP" "LV3"]
   "Toyota Tian Jin" ["LTV"]
   "Guangqi Honda, China" ["LUC"]
   "Ford Chang An" ["LVS"]
   "Chery, China" ["LVV"]
   "Dong Feng Sokon Motor Company (DFSK)" ["LVZ"]
   "MAN China" ["LZM"]
   "Isuzu Guangzhou, China" ["LZE"]
   "Shaanxi Automobile Group, China" ["LZG"]
   "Zhongshan Guochi Motorcycle (Baotian)" ["LZP"]
   "Yutong Zhengzhou, China" ["LZY"]
   "Chongqing Shuangzing Mech & Elec (Howo)" ["LZZ"]
   "Xingyue Group (motorcycles)" ["L4B"]
   "KangDi (ATV)" ["L5C"]
   "Zhejiang Yongkang Easy Vehicle" ["L5K"]
   "Zhejiang Taotao, China (ATV & motorcycles)" ["L5N"]
   "Merato Motorcycle Taizhou Zhongneng" ["L5Y"]
   "Zhejiang Yongkang Huabao Electric Appliance" ["L85"]
   "Zhejiang Summit Huawin Motorcycle" ["L8X"]
   "Mahindra & Mahindra" ["MA1" "MAB" "MAC"]
   "Ford India" ["MAJ"]
   "Honda Siel Cars India" ["MAK"]
   "Hyundai India" ["MAL"]
   "Tata Motors" ["MAT"]
   "Suzuki India (Maruti)" ["MA3" "MBH"]
   "GM India" ["MA6" "MCB"]
   "Mitsubishi India (formerly Honda)" ["MA7"]
   "Suzuki India Motorcycles" ["MB8"]
   "Toyota India" ["MBJ"]
   "Mercedes-Benz India" ["MBR"]
   "Ashok Leyland" ["MB1"]
   "Fiat India" ["MCA"]
   "Volvo Eicher commercial vehicles limited." ["MC2"]
   "Nissan India" ["MDH"]
   "Bajaj Auto" ["MD2"]
   "Shuttle Cars India" ["MD9"]
   "Daimler India Commercial Vehicles" ["MEC"]
   "Renault India" ["MEE"]
   "Volkswagen India" ["MEX"]
   "Toyota Indonesia" ["MHF"]
   "Honda Indonesia" ["MHR"]
   "Suzuki Thailand" ["MLC" "MMS"]
   "Iran Khodro (Peugeot Iran)" ["NAA"]
   "Pars Khodro" ["NAP"]
   "Honda Thailand" ["MLH" "MRH"]
   "Mitsubishi Thailand" ["MMA" "MMB" "MMC" "MMT"]
   "Chevrolet Thailand" ["MMM"]
   "Holden Thailand" ["MMU"]
   "Mazda Thailand" ["MM8"]
   "Ford Thailand" ["MNB"]
   "Nissan Thailand" ["MNT"]
   "Isuzu Thailand" ["MPA" "MP1"]
   "Toyota Thailand" ["MR0"]
   "SSS MOTORS Myanmar" ["MS0"]
   "Suzuki Myanmar Motor Co.,Ltd." ["MS3"]
   "Honda Türkiye" ["NLA"]
   "Mercedes-Benz Türk Truck" ["NLE"]
   "Hyundai Assan" ["NLH"]
   "OTOKAR" ["NLR"]
   "TEMSA" ["NLT"]
   "Mercedes-Benz Türk Buses" ["NMB"]
   "BMC" ["NMC"]
   "Ford Turkey" ["NM0"]
   "Tofaş Türk" ["NM4"]
   "Toyota Türkiye" ["NMT"]
   "Isuzu Turkey" ["NNA"]
   "Ford Philippines" ["PE1"]
   "Mazda Philippines" ["PE3"]
   "Proton, Malaysia" ["PL1"]
   "NAZA, Malaysia (Peugeot)" ["PNA"]
   "Evoke Electric Motorcycles HK" ["R2P"]
   "Steyr Trucks International FZE, UAE" ["RA1"]
   "Kymco, Taiwan" ["RFB"]
   "Sanyang SYM, Taiwan" ["RFG"]
   "Adly, Taiwan" ["RFL"]
   "CPI, Taiwan" ["RFT"]
   "Aeon Motor, Taiwan" ["RF3"]
   "Optare" ["SAB"]
   "Jaguar (F-Pace, I-Pace)" ["SAD"]
   "Land Rover" ["SAL"]
   "Jaguar" ["SAJ"]
   "Rover" ["SAR"]
   "Austin-Rover" ["SAX"]
   "Toyota UK" ["SB1"]
   "McLaren" ["SBM"]
   "Rolls Royce" ["SCA"]
   "Bentley" ["SCB"]
   "Lotus Cars" ["SCC"]
   "DeLorean Motor Cars N. Ireland (UK)" ["SCE"]
   "Aston" ["SCF"]
   "iFor Williams" ["SCK"]
   "Peugeot UK (formerly Talbot)" ["SDB"]
   "General Motors Luton Plant" ["SED"]
   "LDV" ["SEY"]
   "Ford UK" ["SFA"]
   "Alexander Dennis UK" ["SFD"]
   "Honda UK" ["SHH" "SHS"]
   "Nissan UK" ["SJN"]
   "Vauxhall" ["SKF"]
   "JCB Research UK" ["SLP"]
   "Triumph Motorcycles" ["SMT"]
   "Fiat Auto Poland" ["SUF"]
   "FSC (Poland)" ["SUL"]
   "FSO-Daewoo (Poland)" ["SUP"]
   "Solaris Bus & Coach (Poland)" ["SU9" "SUU"]
   "TA-NO (Poland)" ["SWV"]
   "Micro Compact Car AG (smart 1998-1999)" ["TCC"]
   "QUANTYA Swiss Electric Movement (Switzerland)" ["TDM"]
   "SOR buses (Czech Republic)" ["TK9"]
   "Hyundai Motor Manufacturing Czech" ["TMA"]
   "Škoda (Czech Republic)" ["TMB"]
   "Karosa (Czech Republic)" ["TMK" "TN9"]
   "Škoda trolleybuses (Czech Republic)" ["TMP" "TM9"]
   "Tatra (Czech Republic)" ["TMT"]
   "TAZ" ["TNE"]
   "Ikarus Bus" ["TRA" "TSB"]
   "Audi Hungary" ["TRU"]
   "Ikarus Egyedi Autobuszgyar, (Hungary)" ["TSE"]
   "Suzuki Hungary" ["TSM"]
   "Toyota Caetano Portugal" ["TW1"]
   "Mitsubishi Trucks Portugal" ["TYA" "TYB"]
   "Renault Dacia, (Romania)" ["UU1"]
   "Oltcit" ["UU2"]
   "ARO" ["UU3"]
   "Roman SA" ["UU4"]
   "Rocar" ["UU5"]
   "Daewoo Romania" ["UU6"]
   "Euro Bus Diamond" ["UU7"]
   "Astra Bus" ["UU9"]
   "UTB (Uzina de Tractoare Brașov)" ["UZT"]
   "Kia Motors Slovakia" ["U5Y" "U6Y"]
   "Magna Steyr Puch" ["VAG"]
   "MAN Austria" ["VAN"]
   "KTM (Motorcycles)" ["VBK"]
   "Renault" ["VF1", "VF2"]
   "Peugeot" ["VF3"]
   "Talbot" ["VF4"]
   "Renault (Trucks & Buses)" ["VF6"]
   "Citroën" ["VF7"]
   "Matra" ["VF8"]
   "Bugatti" ["VF9"]
   "MBK (motorcycles)" ["VG5"]
   "Scania France" ["VLU"]
   "SOVAB (France)" ["VN1"]
   "Irisbus (France)" ["VNE"]
   "Toyota France" ["VNK"]
   "Renault-Nissan" ["VNV"]
   "Mercedes-Benz Spain" ["VSA"]
   "Suzuki Spain (Santana Motors)" ["VSE"]
   "Nissan Spain" ["VSK" "VWA"]
   "SEAT" ["VSS"]
   "Opel Spain" ["VSX"]
   "Ford Spain" ["VS6"]
   "Citroën Spain" ["VS7"]
   "Carrocerias Ayats (Spain)" ["VS9"]
   "Derbi (motorcycles)" ["VTH"]
   "Yamaha Spain (motorcycles)" ["VTL"]
   "Suzuki Spain (motorcycles)" ["VTT"]
   "TAURO Spain" ["VV9"]
   "Volkswagen Spain" ["VWV"]
   "Zastava / Yugo Serbia" ["VX1"]
   "Neoplan" ["WAG"]
   "Audi" ["WAU"]
   "Audi SUV" ["WA1"]
   "BMW" ["WBA" "WBW" "WBY"]
   "BMW M" ["WBS"]
   "Daimler" ["WDA"]
   "Mercedes-Benz" ["WDB" "WDD"]
   "DaimlerChrysler" ["WDC"]
   "Mercedes-Benz (commercial vehicles)" ["WDF"]
   "Evobus GmbH (Mercedes-Bus)" ["WEB"]
   "Iveco Magirus" ["WJM"]
   "Ford Germany" ["WF0"]
   "Fahrzeugwerk Bernard Krone (truck trailers)" ["WKE"]
   "Kässbohrer/Setra" ["WKK"]
   "MAN Germany" ["WMA"]
   "smart" ["WME"]
   "MINI" ["WMW"]
   "Mercedes-AMG" ["WMX"]
   "Porsche" ["WP0"]
   "Porsche SUV" ["WP1"]
   "Schmitz-Cargobull (truck trailers)" ["WSM"]
   "RUF" ["W09"]
   "Opel" ["W0L"]
   "Opel (since 2017)" ["W0V"]
   "Audi Sport GmbH (formerly quattro GmbH)" ["WUA"]
   "Volkswagen MPV/SUV" ["WVG"]
   "Volkswagen" ["WVW"]
   "Volkswagen Commercial Vehicles" ["WV1"]
   "Volkswagen Bus/Van" ["WV2"]
   "Volkswagen Trucks" ["WV3"]
   "Volvo (NedCar)" ["XLB"]
   "Scania Netherlands" ["XLE"]
   "DAF (trucks)" ["XLR"]
   "Spyker" ["XL9"]
   "Mitsubishi (NedCar)" ["XMC"]
   "VDL Bus & Coach" ["XMG"]
   "Lada/AvtoVAZ (Russia)" ["XTA"]
   "KAMAZ (Russia)" ["XTC"]
   "GAZ (Russia)" ["XTH"]
   "UAZ/Sollers (Russia)" ["XTT"]
   "Trolza (Russia)" ["XTU"]
   "LiAZ (Russia)" ["XTY"]
   "General Motors Russia" ["XUF"]
   "AvtoTor (Russia, General Motors SKD)" ["XUU"]
   "Volkswagen Group Russia" ["XW8"]
   "UZ-Daewoo (Uzbekistan)" ["XWB"]
   "AvtoTor (Russia, Hyundai-Kia SKD)" ["XWE"]
   "PAZ (Russia)" ["X1M"]
   "AvtoTor (Russia, BMW SKD)" ["X4X"]
   "Renault AvtoFramos (Russia)" ["X7L"]
   "Hyundai TagAZ (Russia)" ["X7M"]
   "Volkswagen Belgium" ["YBW"]
   "Volvo Trucks Belgium" ["YB1"]
   "Mazda Belgium" ["YCM"]
   "Van Hool (buses)" ["YE2"]
   "BRP Finland (Lynx snowmobiles)" ["YH2"]
   "Saab-Valmet Finland" ["YK1"]
   "Cadillac (Saab)" ["YSC"]
   "Scania AB" ["YS2"]
   "Saab" ["YS3"]
   "Scania Bus" ["YS4"]
   "Saab NEVS" ["YTN"]
   "Koenigsegg" ["YT9"]
   "Carvia" ["YT9"]
   "Husaberg (motorcycles)" ["YU7"]
   "Polestar (Volvo) (Sweden)" ["YVV" "LPS"]
   "Volvo Cars" ["YV1" "YV4"]
   "Volvo Trucks" ["YV2"]
   "Volvo Buses" ["YV3"]
   "MAZ (Belarus)" ["Y3M"]
   "Zaporozhets/AvtoZAZ (Ukraine)" ["Y6D"]
   "Autobianchi" ["ZAA"]
   "Maserati" ["ZAM"]
   "Piaggio/Vespa/Gilera" ["ZAP"]
   "Alfa Romeo" ["ZAR"]
   "Benelli" ["ZBN"]
   "Cagiva SpA / MV Agusta" ["ZCG"]
   "Iveco" ["ZCF"]
   "Honda Italia Industriale SpA" ["ZDC"]
   "Ducati Motor Holdings SpA" ["ZDM"]
   "Ferrari Dino" ["ZDF"]
   "Yamaha Italy" ["ZD0"]
   "Beta Motor" ["ZD3"]
   "Aprilia" ["ZD4"]
   "Fiat" ["ZFA"]
   "Fiat V.I." ["ZFC"]
   "Ferrari" ["ZFF"]
   "Moto Guzzi" ["ZGU"]
   "Lamborghini" ["ZHW"]
   "Malaguti" ["ZJM"]
   "Innocenti" ["ZJN"]
   "Husqvarna Motorcycles Italy" ["ZKH"]
   "Lancia" ["ZLA"]
   "Marussia (Russia)" ["Z8M"]
   "Dodge" ["1B3" "1D3"]
   "Chrysler" ["1C3" "1C4" "1C6"]
   "Ford Motor Company" ["1FA""1FB" "1FD" "1FM" "1FT"]
   "FWD Corp." ["1F9"]
   "General Motors USA" ["1GA" "1GB" "1GD" "1GE" "1GF" "1GG"]
   "Chevrolet Truck USA" ["1GC"]
   "GMC Truck USA" ["1GT"]
   "Chevrolet USA" ["1G1"]
   "Pontiac USA" ["1G2" "1GM"]
   "Oldsmobile USA" ["1G3"]
   "Buick USA" ["1G4"]
   "Cadillac USA" ["1G6" "1GY"]
   "Saturn USA" ["1G8"]
   "Honda USA" (remove #{"1HD" "1HT"} (ids "1H"))
   "Harley-Davidson" ["1HD"]
   "International Truck and Engine Corp. USA" ["1HT"]
   "Jeep" ["1J4" "1J8"]
   "Lincoln USA" (ids "1L")
   "Mercury USA" ["1ME"]
   "Mack Truck USA" ["1M1" "1M2" "1M3" "1M4"]
   "Mynatt Truck & Equipment" ["1M9"]
   "Nissan USA" (remove #{"1NX"} (conj (ids "1N") "5N1"))
   "NUMMI USA" ["1NX"]
   "Plymouth USA" ["1P3"]
   "John Deere USA" ["1PY"]
   "Roadrunner Hay Squeeze USA" ["1R9"]
   "Volkswagen USA" ["1VW"]
   "Kenworth USA" ["1XK"]
   "Peterbilt USA" ["1XP"]
   "Mazda USA (AutoAlliance International)" ["1YV"]
   "Ford (AutoAlliance International)" ["1ZV"]
   "Chrysler Canada" ["2A4" "2C3"]
   "Bombardier Recreational Products" ["2BP"]
   "Dodge Canada" ["2B3" "2B7" "2D3"]
   "CAMI" ["2CN"]
   "Ford Motor Company Canada" ["2FA" "2FB" "2FC" "2FM" "2FT"]
   "Freightliner" ["1FU" "1FV" "2FU" "2FV"]
   "Sterling" ["2FZ"]
   "General Motors Canada" (ids "2G" [\A \Z])
   "Chevrolet Canada" ["2G1"]
   "Pontiac Canada" ["2G2"]
   "Oldsmobile Canada" ["2G3"]
   "Buick Canada" ["2G4"]
   "mfr. of less than 1000/ yr. Canada" ["2G9"]
   "Honda Canada" ["2HG" "2HK" "2HJ"]
   "Hyundai Canada" ["2HM"]
   "Mercury" (into (ids "2M") (ids "4M"))
   "Nova Bus Canada" ["2NV"]
   "Plymouth Canada" ["2P3"]
   "Toyota Canada" (remove #{"2TP"} (ids "2T"))
   "Triple E Canada LTD" ["2TP"]
   "Volkswagen Canada" ["2V4" "2V8"]
   "Western Star" ["2WK" "2WL" "2WM"]
   "Chrysler Mexico" ["3C4"]
   "RAM Mexico" ["3C6"]
   "Dodge Mexico" ["3D3" "3D4"]
   "Ford Motor Company Mexico" ["3FA" "3FE"]
   "General Motors Mexico" (ids "3G")
   "Honda Mexico" (ids "3H")
   "BRP Mexico (all-terrain vehicles)" ["3JB"]
   "Mazda Mexico" ["3MD" "3MZ"]
   "Nissan Mexico" (remove #{"3NS" "3NE"} (ids "3N"))
   "Polaris Industries USA" ["3NS" "3NE"]
   "Plymouth Mexico" ["3P3"]
   "Volkswagen Mexico" ["3VW"]
   "Federal Motors Inc. USA" ["46J"]
   "Emergency One USA" ["4EN"]
   "Mazda USA" (ids "4F")
   "Mercedes-Benz USA" ["4JG"]
   "Pierce Manufacturing Inc. USA" ["4P1"]
   "Nova Bus USA" ["4RK"]
   "Subaru-Isuzu Automotive" (ids "4S")
   "Lumen Motors" ["4T9"]
   "Arctic Cat Inc." ["4UF"]
   "BMW USA" ["4US"]
   "Frt-Thomas Bus" ["4UZ"]
   "Volvo" ["4V1" "4V2" "4V3" "4V4" "4V5" "4V6" "4VL" "4VM" "4VZ"]
   "Zero Motorcycles (USA)" ["538"]
   "Honda USA-Alabama" (ids "5F")
   "Honda USA-Ohio" (ids "5J")
   "Lincoln" (ids "5L")
   "Hyundai USA" ["5NP"]
   "Toyota USA - trucks" (ids "5T")
   "Tesla, Inc." ["5YJ"]
   "Indian Motorcycle USA" ["56K"]
   "MAN Australia" ["6AB"]
   "Nissan Motor Company Australia" ["6F4"]
   "Kenworth Australia" ["6F5"]
   "Ford Motor Company Australia" ["6FP"]
   "General Motors-Holden (post Nov 2002)" ["6G1"]
   "Pontiac Australia (GTO & G8)" ["6G2"]
   "General Motors-Holden (pre Nov 2002)" ["6H8"]
   "Mitsubishi Motors Australia" ["6MM"]
   "Toyota Motor Corporation Australia" ["6T1"]
   "Privately Imported car in Australia" ["6U9"]
   "Peugeot Argentina" ["8AD"]
   "Ford Motor Company Argentina" ["8AF"]
   "Chevrolet Argentina" ["8AG"]
   "Toyota Argentina" ["8AJ"]
   "Suzuki Argentina" ["8AK"]
   "Fiat Argentina" ["8AP"]
   "Volkswagen Argentina" ["8AW"]
   "Renault Argentina" ["8A1"]
   "Peugeot Chile" ["8GD"]
   "Chevrolet Chile" ["8GG"]
   "Chevrolet Ecuador" ["8LD"]
   "Citroën Brazil" ["935"]
   "Peugeot Brazil" ["936"]
   "Honda Brazil" ["93H"]
   "Toyota Brazil" ["93R" "9BR"]
   "Audi Brazil" ["93U" "93V"]
   "Mitsubishi Motors Brazil" ["93X"]
   "Renault Brazil" ["93Y"]
   "Nissan Brazil" ["94D"]
   "Ford Motor Company Brazil" ["9BF"]
   "Chevrolet Brazil" ["9BG"]
   "Mercedes-Benz Brazil" ["9BM"]
   "Scania Brazil" ["9BS"]
   "Volkswagen Brazil" ["9BW"]
   "Renault Colombia" ["9FB"]
   "BMW Motorrad of North America" ["WB1"]})

(defn- degroup
  [m]
  (reduce-kv (fn [acc k coll]
               (into acc (map #(vector % k)) coll))
             {}
             m))

(s/def :vehiclj.manufacturer/name string?)

(s/fdef manufacturer
  :args (s/cat :id :vehiclj.manufacturer/id)
  :ret (s/nilable :vehiclj.manufacturer/name))

(def ^{:arglists '([id])} manufacturer
  "Return the manufacturer name associated with a manufacturer id."
  (degroup manufacturers))

(defn decode-manufacturer
  "Decode manufacturer information from a vehicle entity."
  [{:iso-3779/keys [wmi vin] :as _vehicle}]
  (let [region (region wmi)
        country (country wmi)
        id (if (= (last wmi) \9) (str wmi "/" (subs vin 11 14)) wmi)
        name (manufacturer id)]
    (cond-> {:vehiclj.manufacturer/id id}
      region (assoc :vehiclj.manufacturer/region region)
      country (assoc :vehiclj.manufacturer/country country)
      name (assoc :vehiclj.manufacturer/name name))))

(defn decode-vin
  "Decode a valid Vehicule Identification Number (VIN) into a vehicle
  data map."
  [vin]
  (let [vehicle #:iso-3779{:vin vin
                           :wmi (subs vin 0 3)
                           :vds (subs vin 3 9)
                           :vis (subs vin 9 17)}
        manufacturer (decode-manufacturer vehicle)]
    (if manufacturer
      (assoc vehicle :vehiclj/manufacturer manufacturer)
      vehicle)))

(s/def :vehiclj/manufacturer
  (s/keys :req [:vehiclj.manufacturer/id]
          :opt [:vehiclj.manufacturer/region
                :vehiclj.manufacturer/country]))

(s/def :vehiclj/vehicle
  (s/with-gen
    (s/and
     (s/keys :req [:iso-3779/vin :iso-3779/wmi :iso-3779/vds :iso-3779/vis]
             :opt [:vehiclj/manufacturer])
     #(= (:iso-3779/vin %)
         (str (:iso-3779/wmi %) (:iso-3779/vds %) (:iso-3779/vis %))))
    #(gen/fmap decode-vin (s/gen :iso-3779/vin))))

(s/fdef decode-manufacturer
  :args (s/cat :vehicle :vehiclj/vehicle)
  :ret :vehiclj/manufacturer)

(s/fdef decode-vin
  :args (s/cat :vin :iso-3779/vin)
  :ret :vehiclj/vehicle
  :fn (s/and #(= (-> % :ret :iso-3779/vin) (-> % :args :vin))))
