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

(defn command [command-kw & params]
  [:sync
   (let [body [:packet-type :common-command :command command-kw]]
     (apply array-map
            (if (seq params)
              (concat body params)
              body)))])

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
                                      0x05 0x70 0x08 0x38])
