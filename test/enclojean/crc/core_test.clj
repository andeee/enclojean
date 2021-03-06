(ns enclojean.crc.core-test
  (:use midje.sweet)
  (:require [enclojean.crc.core :refer [calc-crc8]]
            [enclojean.bytes :as bytes]))

(facts "about `calc-crc8`"
  (fact "calculated crc of empty or 0 is 0"
    (calc-crc8 (bytes/from-seq [0x00])) => 0x00
    (calc-crc8 (bytes/from-seq []))     => 0x00)
  (tabular
   (fact "calculated crc of test vectors is valid"
     (calc-crc8 (bytes/from-seq ?test-vector)) => ?expected)
   ; extracted from http://blog.xivo.io/public/smbus_pec_crc8_test_vectors.txt
   ?test-vector          ?expected
   [0x01]                0x07
   [0x02]                0x0E
   [0x03]                0x09
   [0x04]                0x1C
   [0x00 0x00]           0x00
   [0x00 0x01]           0x07
   [0x00 0x02]           0x0E
   [0x00 0x04]           0x1C
   [0x00 0x08]           0x38
   [0x00 0x10]           0x70
   [0x00 0x20]           0xE0
   [0x00 0x40]           0xC7
   [0x00 0x80]           0x89
   [0x00 0xFF]           0xF3
   [0x01 0x01]           0x12
   [0x02 0x01]           0x2D
   [0x04 0x01]           0x53
   [0x08 0x01]           0xAF
   [0x10 0x01]           0x50
   [0x20 0x01]           0xA9
   [0x40 0x01]           0x5C
   [0x80 0x01]           0xB1
   [0x01 0x00 0x01]      0x6C
   [0x02 0x00 0x01]      0xD1
   [0x04 0x00 0x01]      0xAC
   [0x08 0x00 0x01]      0x56
   [0x10 0x00 0x01]      0xA5
   [0x20 0x00 0x01]      0x44
   [0x40 0x00 0x01]      0x81
   [0x80 0x00 0x01]      0x0C
   [0x61 0x62 0x63]      0x5F
   [0x00 0x07 0x07 0x01] 0x7A
   [0x00 0x01 0x00 0x05] 0x70
   [0x30 0x31 0x32 0x33
    0x34 0x35 0x36 0x37
    0x30 0x31 0x32 0x33
    0x34 0x35 0x36 0x37
    0x30 0x31 0x32 0x33
    0x34 0x35 0x36 0x37
    0x30 0x31 0x32 0x33
    0x34 0x35 0x36 0x37] 0xE4
   [0x24 0x3F 0x6A 0x88
    0x85 0xA3 0x08 0xD3
    0x13 0x19 0x8A 0x2E
    0x03 0x70 0x73 0x44
    0xA4 0x09 0x38 0x22
    0x29 0x9F 0x31 0xD0
    0x08 0x2E 0xFA 0x98
    0xEC 0x4E 0x6C 0x89] 0xCB
   [0x6A 0x6B 0x69 0x6A
    0x6B 0x6C 0x6A 0x6B
    0x6C 0x6D 0x6B 0x6C
    0x6D 0x6E 0x6C 0x6D
    0x6E 0x6F 0x6D 0x6E
    0x6F 0x70 0x6E 0x6F
    0x70 0x71]           0x1B
   [0xe3 0x81 0x93 0xe3
    0x82 0x93 0xe3 0x81
    0xab 0xe3 0x81 0xa1
    0xe3 0x81 0xaf]      0x4C
   [0xF6 0x30 0xFE 0xFF
    0xFE 0xBC 0x30 0x01
    0xFF 0xFF 0xFF 0xFF
    0x2D 0x00]           0xC6))
