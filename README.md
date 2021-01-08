# road.vehicle

[![Clojars Project](https://img.shields.io/clojars/v/fr.reuz/road.vehicle.svg)](https://clojars.org/fr.reuz/road.vehicle)

A Clojure library related to vehicle identification and registration.

This allows manipulating vehicle registration records and reasoning
over various vehicle types.

## Usage

The main service provided by this project is a [Vehicle Identifier Number (VIN)](https://www.wikipedia.org/wiki/Vehicle_identification_number) decoder which given a 17 characters uniquely identifying a vehicle
`road.vehicle/decode` will return data about the manufacturer and the vehicle model.

```clojure
(require '[road.vehicle :as vehicle])

(vehicle/decode "VF1KG1P5E3R488860")
;; ↪ #::vehicle{:vin "VF1KG1P5E3R488860"
;;              :wmi "VF1"
;;              :vds "KG1P5E"
;;              :vis "3R488860"
;;              :manufacturer …
;;              :model …
;;              …}
```

## License

Copyright © 2020 Mathieu Lirzin

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
