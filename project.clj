(defproject enclojean "0.1.0-SNAPSHOT"
  :description "Clojure EnOcean Library"
  :url "http://github.com/andeee/enclojean"
  :license {:name "Apache Licence, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [gloss "0.2.2"]
                 [byte-streams "0.1.11"]]
  :profiles {:dev
             {:dependencies [[midje "1.6.3"]]
              :plugins [[lein-midje "3.1.1"]]}}
  :plugins [[cider/cider-nrepl "0.7.0-SNAPSHOT"]])
