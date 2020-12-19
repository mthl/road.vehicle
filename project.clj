(defproject fr.reuz/vehiclj "0.1.0-SNAPSHOT"
  :description "Vehicule identification and registration"
  :url "http://github.com/mthl/vehiclj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :deploy-repositories
  [["releases" {:url "https://repo.clojars.org" :creds :gpg}]
   ["snapshots" {:url "https://repo.clojars.org" :creds :gpg}]]
  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.10.1"]]}}
  :repl-options {:init-ns vehiclj.fr.siv})
