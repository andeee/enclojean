(ns enclojean.crc.codec-test
  (:use midje.sweet)
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [encode decode]]
            [gloss.core :refer [compile-frame string header]]
            [gloss.core.codecs :refer [identity-codec]]
            [byte-streams :refer [to-byte-array]]))

(defn encode-crc8 [codec v]
  (-> (compile-frame codec)
      crc8-frame
      (encode v)
      to-byte-array
      bytes/to-seq))

(defn decode-crc8 [codec x]
  (-> (compile-frame codec)
      crc8-frame
      (decode x)))

(facts "about `crc8-frame`"
  (tabular
   (fact "encoding appends crc8 checksum"
     (encode-crc8 :byte ?a-byte) => ?expected)
   ?a-byte ?expected
   0x01    [0x01 0x07]
   0x02    [0x02 0x0E]
   0x03    [0x03 0x09]
   0x04    [0x04 0x1C])
  (fact "decoding checks for crc8 checksum"
    (decode-crc8 :byte (bytes/from-seq [0x01 0x07])) => 0x01
    (decode-crc8 :byte (bytes/from-seq [0x01 0xFF])) => (throws Exception))
  (tabular
   (fact "nested header codec is correctly en/decoded"
     (let [b->h (fn [body]
                  (get
                   {:a 1 :b 2 :c 3}
                   (first body)))
           h->b (fn [hd]
                  (condp = hd
                    1 (compile-frame [:a :int16])
                    2 (compile-frame [:b :float32])
                    3 (compile-frame [:c (string :utf-8 :delimiters [\0])])))
           codec (header :byte h->b b->h)]
       (encode-crc8 codec ?decoded) => (bytes/unchecked-seq ?encoded)
       (decode-crc8 codec (bytes/from-seq ?encoded)) => ?decoded))
   ?decoded   ?encoded
   [:a 1]     [0x01 0x00 0x01 0x6C]
   [:b 2.5]   [0x02 0x40 0x20 0x00 0x00 0x1C]
   [:c "abc"] [0x03 0x61 0x62 0x63 0x30 0xAC])
  (tabular
   (fact "nested identity codec is correctly en/decoded"
     (let [unpack (comp bytes/to-seq to-byte-array)]
       (unpack (decode-crc8 identity-codec (bytes/from-seq ?encoded)))) => ?decoded
       (encode-crc8 identity-codec (bytes/from-seq ?decoded)) => (bytes/unchecked-seq ?encoded))
   ?decoded                   ?encoded
   [0x01]                     [0x01 0x07]
   [0x02]                     [0x02 0x0E]
   [0x03]                     [0x03 0x09]
   [0x04]                     [0x04 0x1C]
   [0x01 0x00 0x01]           [0x01 0x00 0x01 0x6C]
   [0x02 0x40 0x20 0x00 0x00] [0x02 0x40 0x20 0x00 0x00 0x1C]
   [0x03 0x61 0x62 0x63 0x30] [0x03 0x61 0x62 0x63 0x30 0xAC]))
