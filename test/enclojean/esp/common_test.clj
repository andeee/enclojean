(ns enclojean.esp.common-test
  (:use midje.sweet
        enclojean.esp.common)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]))

(defn encode-esp [decoded]
  (-> (encode esp decoded) to-byte-array bytes/to-seq))

(defn decode-esp [telegram]
  (decode esp (to-byte-buffers telegram)))

(def read-version-telegram (bytes/from-seq 
                            [0x55 0x00 0x01 0x00
                             0x05 0x70 0x03 0x09]))

(def read-version-decoded [:sync {:packet-type :common-command 
                                  :command :read-version}])

(def write-reset-telegram (bytes/from-seq 
                            [0x55 0x00 0x01 0x00
                             0x05 0x70 0x02 0x0E]))

(def write-reset-decoded [:sync {:packet-type :common-command 
                                  :command :write-reset}])

(def write-sleep-telegram (bytes/from-seq 
                            [0x55 0x00 0x01 0x00
                             0x05 0x70 0x01 0x00 
                             0x00 0x00 0x01 0x0E]))

(def write-sleep-decoded [:sync {:packet-type :common-command 
                                    :command :write-sleep :sleep-period 1}])

(facts "about common commands"
  (fact "encodes read-version"
    (encode-esp read-version-decoded) => (vec read-version-telegram))
  (fact "decodes read-version"
    (decode-esp read-version-telegram) => read-version-decoded)
  (fact "encodes write-reset"
    (encode-esp write-reset-decoded) => (vec write-reset-telegram))
  (fact "encodes write-sleep"
    (encode-esp write-sleep-decoded) => (vec write-sleep-telegram)))
