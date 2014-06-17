(ns enclojean.core-test
  (:use midje.sweet)
  (:require [enclojean.core :as core]))

(facts "about `calc-crc8`"
  (fact "crc of empty or 0 is 0"
    (core/calc-crc8 (byte-array [(byte 0)])) => 0
    (core/calc-crc8 (byte-array 0)) => 0))
