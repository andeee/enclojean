(ns enclojean.byte-test
  (:use midje.sweet)
  (:require [enclojean.byte :as byte]))

(defn TV [byte-seq]
    (byte/unchecked-byte-array byte-seq))

(facts "about `calc-crc8`"
  (fact "calculated crc of empty or 0 is 0"
    (byte/calc-crc8 (TV [0x00])) => 0
    (byte/calc-crc8 (TV []))     => 0)
  (tabular
   (fact "calculated crc of test vectors is valid"
     (byte/calc-crc8 ?test-vector) => ?expected)
   ; extracted from http://blog.xivo.io/public/smbus_pec_crc8_test_vectors.txt
   ?test-vector            ?expected
   (TV [0x01])             0x07
   (TV [0x02])             0x0E
   (TV [0x03])             0x09
   (TV [0x04])             0x1C
   (TV [0x00 0x00])        0x00
   (TV [0x00 0x01])        0x07
   (TV [0x00 0x02])        0x0E
   (TV [0x00 0x04])        0x1C
   (TV [0x00 0x08])        0x38
   (TV [0x00 0x10])        0x70
   (TV [0x00 0x20])        0xE0
   (TV [0x00 0x40])        0xC7
   (TV [0x00 0x80])        0x89
   (TV [0x00 0xFF])        0xF3
   (TV [0x01 0x01])        0x12
   (TV [0x02 0x01])        0x2D
   (TV [0x04 0x01])        0x53
   (TV [0x08 0x01])        0xAF
   (TV [0x10 0x01])        0x50
   (TV [0x20 0x01])        0xA9
   (TV [0x40 0x01])        0x5C
   (TV [0x80 0x01])        0xB1
   (TV [0x01 0x00 0x01])   0x6C
   (TV [0x02 0x00 0x01])   0xD1
   (TV [0x04 0x00 0x01])   0xAC
   (TV [0x08 0x00 0x01])   0x56
   (TV [0x10 0x00 0x01])   0xA5
   (TV [0x20 0x00 0x01])   0x44
   (TV [0x40 0x00 0x01])   0x81
   (TV [0x80 0x00 0x01])   0x0C
   (TV [0x61 0x62 0x63])   0x5F
   (TV
    [0x00 0x07 0x07 0x01]) 0x7A
   (TV
    [0x30 0x31 0x32 0x33
     0x34 0x35 0x36 0x37
     0x30 0x31 0x32 0x33
     0x34 0x35 0x36 0x37
     0x30 0x31 0x32 0x33
     0x34 0x35 0x36 0x37
     0x30 0x31 0x32 0x33
     0x34 0x35 0x36 0x37]) 0xE4
   (TV
    [0x24 0x3F 0x6A 0x88
     0x85 0xA3 0x08 0xD3
     0x13 0x19 0x8A 0x2E
     0x03 0x70 0x73 0x44
     0xA4 0x09 0x38 0x22
     0x29 0x9F 0x31 0xD0
     0x08 0x2E 0xFA 0x98
     0xEC 0x4E 0x6C 0x89]) 0xCB
   (TV
    [0x6A 0x6B 0x69 0x6A
     0x6B 0x6C 0x6A 0x6B
     0x6C 0x6D 0x6B 0x6C
     0x6D 0x6E 0x6C 0x6D
     0x6E 0x6F 0x6D 0x6E
     0x6F 0x70 0x6E 0x6F
     0x70 0x71])           0x1B
   (TV
    [0xe3 0x81 0x93 0xe3
     0x82 0x93 0xe3 0x81
     0xab 0xe3 0x81 0xa1
     0xe3 0x81 0xaf])      0x4C
   (TV
    [0xF6 0x30 0xFE 0xFF
     0xFE 0xBC 0x30 0x01
     0xFF 0xFF 0xFF 0xFF
     0x2D 0x00])           0xC6))

