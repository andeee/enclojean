(ns enclojean.esp.common-test
  (:use midje.sweet
        enclojean.esp.common)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.test-utils :refer [TV TV->buf]]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]))

(def read-version-telegram (TV [0x55 0x00 0x01 0x00
                                0x05 0x70 0x03 0x09]))

(facts "about common commands"
  (fact "encodes co_rd_version"
    (vec (to-byte-array (encode esp [:sync {:packet-type :common-command :command 0x03}]))) =>
    (vec read-version-telegram))
  (fact "decodes co_rd_version"
    (decode esp (to-byte-buffers read-version-telegram)) =>
    [:sync {:packet-type :common-command :command 0x03}]))
