(ns enclojean.crc.codec-test
  (:use midje.sweet)
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [enclojean.test-utils :refer [TV]]
            [gloss.io :refer [encode decode]]
            [gloss.core :refer [compile-frame]]
            [byte-streams :refer [to-byte-array]]))

(defn encode-crc8 [v]
  (-> (compile-frame :byte)
      crc8-frame
      (encode v)
      to-byte-array
      vec))

(defn decode-crc8 [x]
  (-> (compile-frame :byte)
      crc8-frame
      (decode x)))

(facts "about `crc8-frame`"
  (tabular
   (fact "encoding appends crc8 checksum"
     (encode-crc8 ?a-byte) => ?expected)
   ?a-byte ?expected
   0x01    [0x01 0x07]
   0x02    [0x02 0x0E]
   0x03    [0x03 0x09]
   0x04    [0x04 0x1C])
  (fact "decoding checks for crc8 checksum"
    (decode-crc8 (TV [0x01 0x07])) => 0x01
    (decode-crc8 (TV [0x01 0xFF])) => (throws Exception)))
