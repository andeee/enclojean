(ns enclojean.esp.core-test
  (:use midje.sweet)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-array]]))

(def rocker-switch-telegram (bytes/from-seq 
                             [0x55 0x00 0x07 0x07
                              0x01 0x7A 0xF6 0x30
                              0xFE 0xFF 0xFE 0xBC
                              0x30 0x01 0xFF 0xFF
                              0xFF 0xFF 0x2D 0x00
                              0xC6]))

(def ok-response {:packet-type :response :data [(byte 0x00)]})

(def ok-response-telegram (bytes/from-seq [0x55 0x00 0x01 0x00
                                           0x02 0x65 0x00 0x00]))

(facts "about esp codec"
  (fact "decodes a rocker switch telegram"
    (decode esp rocker-switch-telegram) =>
    (just {:packet-type :radio, :data anything, :optional-data anything}))
  (fact "roundtrip encodes/decodes response telegram"
    (decode esp (encode esp ok-response)) => ok-response
    (vec (to-byte-array (encode esp (decode esp ok-response-telegram)))) => 
    (vec ok-response-telegram)))
