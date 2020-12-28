(defproject fr.reuz/road.vehicle "0.1.0-SNAPSHOT"
  :description "Vehicule identification and registration"
  :url "http://github.com/mthl/road.vehicle"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :deploy-repositories
  [["releases" {:url "https://repo.clojars.org" :creds :gpg}]
   ["snapshots" {:url "https://repo.clojars.org" :creds :gpg}]]
  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                        [org.clojure/clojurescript "1.10.758"]
                        [org.clojure/test.check "1.1.0"]]}}
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-doo "0.1.10"]]
  :aliases {"test-cljs" ["doo" "node" "test" "once"]}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/js/test.js"
                                   :output-dir "target/js"
                                   :main road.vehicle.runner
                                   :target :nodejs}}]}
  :repl-options {:init-ns road.vehicle})
