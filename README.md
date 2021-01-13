# road.vehicle

[![Clojars Project](https://img.shields.io/clojars/v/fr.reuz/road.vehicle.svg)](https://clojars.org/fr.reuz/road.vehicle)

A Clojure library related to vehicle identification and registration.

This allows manipulating vehicle registration records and reasoning
over various vehicle types.

## Usage

The main service provided by this project is a [Vehicle Identification Number (VIN)](https://www.wikipedia.org/wiki/Vehicle_identification_number) decoder `road.vehicle/decode` which given a sequence of 17 characters uniquely identifying one specific vehicle, will return various information about that vehicle, its manufacturer and its model.

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

## Data model

<!-- To edit image replace "https://mermaid.ink/svg/" un the URL with "https://mermaid-js.github.io/mermaid-live-editor/#/edit/" -->
<img width=300 alt="Data model" src="https://mermaid.ink/svg/eyJjb2RlIjoiY2xhc3NEaWFncmFtXG4gICAgVmVoaWNsZSAtLT4gTW9kZWwgOiBtb2RlbFxuICAgIFZlaGljbGUgLS0-IE1hbnVmYWN0dXJlciA6IG1hbnVmYWN0dXJlclxuXG4gICAgVmVoaWNsZSA6ICtTdHJpbmcgdmluXG4gICAgVmVoaWNsZSA6ICtTdHJpbmcgd21pXG4gICAgVmVoaWNsZSA6ICtTdHJpbmcgdmRzXG4gICAgVmVoaWNsZSA6ICtTdHJpbmcgdmlzXG4gICAgVmVoaWNsZSA6ICtTdHJpbmcgc2VyaWFsXG5cbiAgICBNb2RlbCA6ICtTdHJpbmcgbmFtZVxuICAgIE1vZGVsIDogK0ludGVnZXIgeWVhclxuXG4gICAgTWFudWZhY3R1cmVyIDogK1N0cmluZyBpZFxuICAgIE1hbnVmYWN0dXJlciA6ICtTdHJpbmcgbmFtZVxuICAgIE1hbnVmYWN0dXJlciA6ICtTdHJpbmcgY291bnRyeVxuICAgIE1hbnVmYWN0dXJlciA6ICtTdHJpbmcgcmVnaW9uXG4gICAgXG4gICAgICAgICAgICAiLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOmZhbHNlfQ">

## License

Copyright © 2020-2021 Mathieu Lirzin

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
