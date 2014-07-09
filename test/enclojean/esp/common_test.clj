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
                                  :command 0x03}])
(facts "about common commands"
  (fact "encodes read-version"
    (encode-esp read-version-decoded) => (vec read-version-telegram))
  (fact "decodes read-version"
    (decode-esp read-version-telegram) => read-version-decoded))
