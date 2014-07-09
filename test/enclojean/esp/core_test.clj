(ns enclojean.esp.core-test
  (:use midje.sweet)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [decode]]))

(def rocker-switch-telegram (bytes/from-seq 
                             [0x55 0x00 0x07 0x07
                              0x01 0x7A 0xF6 0x30
                              0xFE 0xFF 0xFE 0xBC
                              0x30 0x01 0xFF 0xFF
                              0xFF 0xFF 0x2D 0x00
                              0xC6]))

(facts "about esp codec"
  (fact "decodes a rocker switch telegram"
    (second (decode esp rocker-switch-telegram)) =>
    (just {:packet-type :radio, :data anything, :optional-data anything})))
