(ns enclojean.bytes-test
  (:use midje.sweet)
  (:require [enclojean.bytes :as bytes]))

(fact "bytes/from-hex creates an unchecked byte-array from a string"
  (seq (bytes/from-hex "00")) => [0]
  (seq (bytes/from-hex "00 FF")) => [0 -1])
