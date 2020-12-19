(ns vehiclj.fr.siv
  "French specific vehicle identification.

  French specific vehicle types are described in the following link:
  <https://www.legifrance.gouv.fr/jorf/article_jo/JORFARTI000032675226>"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as str]))

(s/def ::genre
  ;; The valid abbreviations of “genre national” which is a french specific
  ;; vehicle type.
  #{"CAM"
    "CL"
    "CTTE"
    "CYCL"
    "MAGA"
    "MIAR"
    "MTL"
    "MTT1"
    "MTT2"
    "QM"
    "REA"
    "REM"
    "RESP"
    "RETC"
    "SRAT"
    "SREA"
    "SREM"
    "SRSP"
    "SRTC"
    "TCP"
    "TM"
    "TRA"
    "TRR"
    "VASP"
    "VP"})

(s/fdef genres
  :args (s/cat :abbrev ::genre)
  :ret string?)

(def genres
  "Map associating “genre national” abbreviations to their description.

  The description defines the semantics attached to the abbreviation."
  {"CAM" (str
          "Camions (véhicules d'un poids total autorisé en charge excédant "
          "3 500 kg autres que les tracteurs routiers)")
   "CL" "Cyclomoteurs à deux roues ou cyclo-moteurs non carrossés à trois roues"
   "CTTE" (str
           "Camionnettes (véhicules d'un poids total autorisé en charge "
           "inférieur ou égal à 3 500 kg autres que les tracteurs routiers)")
   "CYCL" "Cyclomoteurs à trois roues"
   "MAGA" "Machines agricoles automotrices"
   "MIAR" "Machines et instruments remorqués"
   "MTL" "Motocyclettes légères"
   "MTT1" (str
           "Motocyclettes autres que motocyclettes légères, dont la puissance "
           "maximale nette CE n'excède pas 25 kW et dont la puissance maximale"
           " nette CE/poids en ordre de marche n'excède pas 0,16 kW/kg")
   "MTT2" "Autres motocyclettes"
   "QM" "Quadricycles à moteur"
   "REA" "Remorques agricoles"
   "REM" "Remorques routières"
   "RESP" "Remorques spécialisées"
   "RETC" "Remorques pour transports combinés"
   "SRAT" "Semi-remorques avant-train"
   "SREA" "Semi-remorques agricoles"
   "SREM" "Semi-remorques routières"
   "SRSP" "Semi-remorques spécialisées"
   "SRTC" "Semi-remorques pour transports combinés"
   "TCP" "Transports en commun de personnes"
   "TM" "Tricycles à moteur"
   "TRA" "Tracteurs agricoles"
   "TRR" "Tracteurs routiers"
   "VASP" "Véhicules automoteur spécialisés"
   "VP" "Voitures particulières"})

(s/def ::carrosserie
  ;; The valid abbreviations of “genre national” which is a french specific
  ;; vehicle type.
  #{"AGRICOLE"
    "AMBULANC"
    "AR_FORES"
    "AR_TRAIN"
    "ATELIER"
    "AV_TRAIN"
    "BACHE"
    "BAZ_FOR"
    "BENNE"
    "BEN_AMO"
    "BEN_CERE"
    "BETAIL"
    "BETON"
    "BOM"
    "BREAK"
    "BUGGY"
    "BUS"
    "CABR"
    "CAR"
    "CARAVANE"
    "CARB_LEG"
    "CARB_LRD"
    "CASIERS"
    "CHAR_POR"
    "CHAS_CAB"
    "CI"
    "CIALE"
    "CIT_ALIM"
    "CIT_ALTD"
    "CIT_BETA"
    "CIT_CHIM"
    "CIT_EAU"
    "CIT_GAZ"
    "CIT_PULV"
    "CIT_VID"
    "CLTRM"
    "CLTRP"
    "CYCLM"
    "DEPANNAG"
    "DERIV_VP"
    "ENDURO"
    "FG_BLIND"
    "FG_FUNER"
    "FG_TD"
    "FOREST"
    "FOURGON"
    "GRUE"
    "HANDICAP"
    "INCENDIE"
    "MAGASIN"
    "NON_SPEC"
    "PLATEAU"
    "PLSC"
    "PR_REM"
    "PR_SREM"
    "PTE_BAT"
    "PTE_CONT"
    "PTE_ENG"
    "PTE_FER"
    "PTE_VOIT"
    "QLEM"
    "QLEMM"
    "QLEMP"
    "QLOMM"
    "QLOMP"
    "QUAD"
    "QUADHR"
    "QUADLP1"
    "QUADLP2"
    "SANITAIR"
    "SAVOYARD"
    "SIDE_CAR"
    "SOLO"
    "SOLO_SIDE_CAR"
    "TB"
    "TMM1"
    "TMM2"
    "TMP1"
    "TMP2"
    "TRAVAUX"
    "TRIAL"
    "VOIRIE"
    "VTTE"})

(s/fdef carrosseries
  :args (s/cat :abbrev ::carrosserie)
  :ret string?)

(def carrosseries
  "Map associating “carrosseries” abbreviations to their description.

  The description defines the semantics attached to the abbreviation."
  {"SOLO" "Motocyclettes sans side-car (solo)"
   "ENDURO" "Motocyclettes d'enduro"
   "TRIAL" "Motocyclettes de trial"
   "SOLO_SIDE_CAR" "Motocyclettes avec side-car adjoint"
   "SIDE_CAR" (str
               "Motocyclettes avec side-car intégré (véhicule à trois roues"
               " non symétriques)")
   "TMP1" (str
           "Tricycles dont le poids à vide n'excède pas 550 kg et dont la "
           "puissance maximale nette CE n'excède pas 15 kW affectés "
           "au transport de personnes")
   "TMP2" "Autres tricycles affectés au transport de personnes"
   "QUAD" "Quad routier léger"
   "QLEM" "Quadricycles légers pour le transport de personnes"
   "QLEMP" "Quadricycles légers pour le transport de personnes"
   "QUADLP1" "Quad routier lourd"
   "QUADLP2" "Quad routier lourd"
   "QUADHR" "Quad tout-terrain lourd"
   "BUGGY" "Buggy côte à côte"
   "QLOMP" "Quadricycles lourds à moteur affectés au transport de personnes"
   "VTTE" "Cyclomoteurs carrossés à trois roues (voiturettes)"
   "CLTRP" (str
            "Cyclomoteurs non carrossés à trois roues pour le transport "
            "de personnes")
   "CI" "Conduite intérieure"
   "CABR" "Cabriolet"
   "BREAK" "Break"
   "CIALE" "Commerciale"
   "HANDICAP" "Handicapés"
   "NON_SPEC" "Divers (non spécifiée)"
   "BUS" "Autobus"
   "CAR" "Autocar"
   "CYCLM" (str
            "Cyclomoteurs carrossés à trois roues affectés au transport"
            " de marchandises")
   "CLTRM" (str
            "Cyclomoteurs non carrossés à trois roues affectés au transport"
            " de marchandises")
   "FOREST" "Forestier"
   "PR_REM" "Pour remorques"
   "PR_SREM" "Pour semi-remorques"
   "BEN_AMO" "Bennes amovibles"
   "BENNE" (str
            "Bennes dont le déchargement est effectué mécaniquement par le "
            "fond à l'aide d'un convoyeur à raclettes, d'une vis sans fin,"
            " etc... ou Bennes basculantes de chantier et de travaux publics")
   "BEN_CERE" "Bennes céréalières"
   "BETAIL" "Bétaillère"
   "CASIERS" "Casiers"
   "CIT_ALIM" "Citerne à produits alimentaires"
   "CIT_ALTD" "Citerne à produit alimentaire à température dirigée"
   "CIT_BETA" "Citerne pour aliments du bétail"
   "CIT_CHIM" "Citerne à produits chimiques"
   "CIT_GAZ" "Citerne à gaz liquéfiés"
   "CARB_LEG" "Citerne à hydrocarbures légers"
   "CARB_LRD" "Citerne à hydrocarbures lourds"
   "CIT_VID" "Citerne à vidange"
   "CIT_EAU" "Citerne à eau"
   "CIT_PULV" "Citerne à produits pulvérulents ou granulaires"
   "BACHE" "Fourgon bâché avec parois rigides"
   "FOURGON" "Fourgon avec parois et toit rigides"
   "FG_TD" "Fourgon à température dirigée"
   "BETON" "Bétonnière"
   "PLATEAU" "Plateau"
   "PTE_BAT" "Porte-bateau"
   "PTE_FER" "Porte-fers"
   "PTE_VOIT" "Porte-voitures"
   "SAVOYARD" "Savoyardes"
   "PLSC" "Carrosserie à parois latérales souples coulissantes"
   "CHAS_CAB" "Châssis-cabine"
   "PTE_ENG" "Porte-engins"
   "PTE_CONT" "Porte-conteneurs ou caisses mobiles ou amovibles"
   "AV_TRAIN" "Avant-train routier"
   "AR_TRAIN" "Arrière-train routier"
   "AR_FORES" "Arrière-train forestier"
   "TB" "Triqueballe"
   "TMM1" (str
           "Tricycles de poids à vide ≤ 550 kg et puissance maximale nette "
           "CE ≤ 15 kw affectés au transport de marchandises")
   "TMM2" "Autres tricycles affectés au transport de marchandises"
   "QLEMM" "Quadricycles légers à moteur"
   "QLOMM" "Quadricycles lourds à moteur affectés au transport de marchandises"
   "AMBULANC" "Ambulance (pour personne couchée)"
   "ATELIER" "Atelier"
   "BAZ_FOR" "Bazar forain"
   "BOM" "Bennes à ordures ménagères"
   "CARAVANE" "Caravane"
   "CHAR_POR" "Chariot porteur"
   "DEPANNAG" "Dépannage"
   "FG_BLIND" "Fourgon blindé"
   "FG_FUNER" "Fourgon funéraire"
   "GRUE" "Grue"
   "INCENDIE" "Incendie"
   "MAGASIN" "Magasin"
   "SANITAIR" "Sanitaire"
   "TRAVAUX" "Travaux publics et industriels"
   "VOIRIE" "Voirie"
   "AGRICOLE" "Agricole"
   "DERIV_VP" "Fourgonnette dérivée de VP"})

(defn cnit-1996?
  "Check if identifier is a valid “Code National d'Identification du
  Type” (CNIT) ?"
  [id]
  (and (string? id)
       (= (count id) 12)))

(defn cnit-2009?
  "Check if identifier is a valid “Code National d'Identification du
  Type” (CNIT) ?"
  [id]
  (and (string? id)
       (= (count id) 15)))

(defn cnit?
  "Check if identifier is a valid “Code National d'Identification du
  Type” (CNIT) ?"
  [id]
  (or (cnit-2009? id)
      (cnit-1996? id)))

(s/def ::cnit
  ;; “Code National d'Identification du Type”
  (s/with-gen cnit?
    #(gen/fmap str/join
               (gen/vector (gen/char-alphanumeric) 15))))
