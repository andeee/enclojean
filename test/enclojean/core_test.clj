(ns enclojean.core-test
  (:use midje.sweet)
  (:require [enclojean.core :refer [esp3-frame]]
            [enclojean.test-utils :refer [TV]]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-buffer to-byte-array]]))


(def rocker-switch-telegram (TV [0x55 0x00 0x07 0x07
                                 0x01 0x7A 0xF6 0x30
                                 0xFE 0xFF 0xFE 0xBC
                                 0x30 0x01 0xFF 0xFF
                                 0xFF 0xFF 0x2D 0x00
                                 0xC6]))

(def read-version-telegram (TV [0x55 0x00 0x01 0x00
                                0x05 0x70 0x03 0x09]))

(facts "about `esp3-frame`"
  (fact "decodes a rocker switch telegram"
    (second (decode esp3-frame rocker-switch-telegram)) =>
    (just {:packet-type :radio, :data anything, :optional-data anything})))

(facts "about common commands"
  (fact "encodes co_rd_version"
    (vec (to-byte-array (encode esp3-frame [:sync {:packet-type :common-command :data (to-byte-buffer (TV [0x03])) :optional-data (to-byte-buffer (TV []))}]))) =>
    (vec read-version-telegram)))
