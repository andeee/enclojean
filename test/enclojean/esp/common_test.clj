(ns enclojean.esp.common-test
  (:use midje.sweet
        enclojean.esp.common)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]))

(defn unchecked-byte-vec [v]
  (vec (bytes/from-seq v)))

(defn encode-esp [decoded]
  (-> (encode esp decoded) to-byte-array bytes/to-seq))

(defn decode-esp [telegram]
  (decode esp (to-byte-buffers (bytes/from-seq telegram))))

(defn gen-encoded [command]
  (vec (map #(symbol (format "0x%02X" %)) (encode-esp command))))

(defn command [command-kw & params]
  (let [body [:packet-type :common-command :command command-kw]]
    (apply array-map
           (if (seq params)
             (concat body params)
             body))))

(tabular
 (facts "about common commands"
   (fact "encode works"
     (encode-esp ?decoded) => (unchecked-byte-vec ?encoded))
   (fact "decode works"
     (decode-esp ?encoded) => ?decoded))
 ?decoded                            ?encoded
 (command :write-sleep
          :sleep-period 1)           [0x55 0x00 0x05 0x00
                                      0x05 0xDB 0x01 0x00 
                                      0x00 0x00 0x01 0x65]
 (command :read-version)             [0x55 0x00 0x01 0x00  
                                      0x05 0x70 0x03 0x09]
 (command :write-reset)              [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x02 0x0E]
 (command :read-sys-log)             [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x04 0x1C]
 (command :write-sys-log)            [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x05 0x1B]
 (command :write-built-in-self-test) [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x06 0x12]
 (command :write-id-base
          :base-id 0xFF800000)       [0x55 0x00 0x05 0x00
                                      0x05 0xDB 0x07 0xFF
                                      0x80 0x00 0x00 0xF3]
 (command :read-id-base)             [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x08 0x38]
 (command :write-repeater-level
          :repeater-enable :on
          :repeater-level :level-2)  [0x55 0x00 0x03 0x00
                                      0x05 0xa6 0x09 0x01
                                      0x02 0x21]
 (command :read-repeater-level)      [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x0a 0x36]
 (command :write-filter-add
          :filter-type :r-org
          :filter-value 0xCAFEBABE
          :filter-kind :apply)       [0x55 0x00 0x07 0x00
                                      0x05 0x0d 0x0b 0x01
                                      0xca 0xfe 0xba 0xbe
                                      0x80 0x9d]
 (command :write-filter-delete
          :filter-type :r-org
          :filter-value 0xcafebabe)  [0x55 0x00 0x06 0x00
                                      0x05 0x66 0x0C 0x01
                                      0xCA 0xFE 0xBA 0xBE
                                      0x01]
 (command :write-filter-delete-all)  [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x0D 0x23]
 (command :write-filter-enable
          :filter-enable :off
          :filter-operator :and)     [0x55 0x00 0x03 0x00
                                      0x05 0xA6 0x0E 0x00
                                      0x01 0x2B]
 (command :read-filters)             [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x0F 0x2D]
 (command :write-wait-maturity
          :wait-end-maturity-enable
          :on)                       [0x55 0x00 0x02 0x00
                                      0x05 0xCD 0x10 0x01
                                      0x50]
 (command :write-subtelegram
          :write-subtelegram-enable
          :off)                      [0x55 0x00 0x02 0x00
                                      0x05 0xCD 0x11 0x00
                                      0x42]
 (command :write-memory
          :memory-type :idata-ram
          :memory-address 0xCAFEBABE
          :memory-data 0x0C)         [0x55 0x00 0x07 0x00
                                      0x05 0x0D 0x12 0x03
                                      0xCA 0xFE 0xBA 0xBE
                                      0x0C 0x84]
 (command :read-memory
          :memory-type :xdata-ram
          :memory-address 0xCAFEBABE
          :data-length 0xFFFF)       [0x55 0x00 0x08 0x00
                                      0x05 0x4A 0x13 0x04
                                      0xCA 0xFE 0xBA 0xBE
                                      0xFF 0xFF 0x4D]
 (command :read-memory-address
          :memory-area
          :system-error-log)         [0x55 0x00 0x02 0x00
                                      0x05 0xCD 0x14 0x02
                                      0x0D]
 (command :read-security)            [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x15 0x6B]
 (command :write-learnmode
          :learn-mode-enable :on
          :timeout 0xCAFEBABE
          :channel 0xAA)             [0x55 0x00 0x06 0x01
                                      0x05 0x73 0x17 0x01
                                      0xCA 0xFE 0xBA 0xBE
                                      0xAA 0x07]
 (command :read-learnmode)           [0x55 0x00 0x01 0x00
                                      0x05 0x70 0x18 0x48]
 (command :write-secure-device-add
          :security-level-format 0x8A
          :device-id 0xCAFEBABE
          :private-key [0x01 0x02 0x03 0x04
                        0x05 0x06 0x07 0x08
                        0x09 0x0A 0x0B 0x0C
                        0x0D 0x0E 0x0F 0x00]
          :rolling-code [0xAB 0xCD 0xEF]
          :direction
          :outbound-table)           [0x55 0x00 0x19 0x01
                                      0x05 0x96 0x19 0x8A
                                      0xCA 0xFE 0xBA 0xBE
                                      0x01 0x02 0x03 0x04
                                      0x05 0x06 0x07 0x08
                                      0x09 0x0A 0x0B 0x0C
                                      0x0D 0x0E 0x0F 0x00
                                      0xAB 0xCD 0xEF 0x01
                                      0x2E]
 (command :write-secure-device-add
          :security-level-format 0x8A
          :device-id 0xCAFEBABE
          :private-key [0x01 0x02 0x03 0x04
                        0x05 0x06 0x07 0x08
                        0x09 0x0A 0x0B 0x0C
                        0x0D 0x0E 0x0F 0x00]
          :rolling-code
          [0xAB 0xCD 0xEF])          [0x55 0x00 0x19 0x00
                                      0x05 0x83 0x19 0x8A
                                      0xCA 0xFE 0xBA 0xBE
                                      0x01 0x02 0x03 0x04
                                      0x05 0x06 0x07 0x08
                                      0x09 0x0A 0x0B 0x0C
                                      0x0D 0x0E 0x0F 0x00
                                      0xAB 0xCD 0xEF 0x62]
(command :read-secure-device-by-index
          :index 0x01
          :direction
          :outbound-table-broadcast) [0x55 0x00 0x02 0x01
                                      0x05 0xD8 0x1B 0x01
                                      0x02 0x55])
