(ns enclojean.esp.core-test
  (:use midje.sweet)
  (:require [enclojean.test-utils :refer :all]))

(def rocker-switch-telegram [0x55 0x00 0x07 0x07
                             0x01 0x7A 0xF6 0x30
                             0xFE 0xFF 0xFE 0xBC
                             0x30 0x01 0xFF 0xFF
                             0xFF 0xFF 0x2D 0x00
                             0xC6])

(def rocker-switch {:packet-type :radio,
                    :data [0xF6 0x30 0xFE 0xFF 0xFE 0xBC 0x30],
                    :optional-data [0x01 0xFF 0xFF 0xFF 0xFF 0x2D 0x00]})

(def ok-response {:packet-type :response :data [0x00] :optional-data nil})

(def ok-response-telegram [0x55 0x00 0x01 0x00
                           0x02 0x65 0x00 0x00])

(defn transform-data [decoded f]
  (reduce (fn [result data]
            (let [[key val] data]
              (if val (assoc result key (f val))
                  result)))
          decoded
          (select-keys decoded [:data :optional-data])))

(defn byte-buffers->data [decoded]
  (transform-data decoded to-byte-seq))

(defn data->byte-buffers [decoded]
  (transform-data decoded unchecked-byte-buffers))

(defn data->byte-vec [decoded]
  (transform-data decoded unchecked-byte-vec))

(facts "about esp codec"
  (tabular
   (fact "roundtrip encodes/decodes telegrams"
     (byte-buffers->data (decode-esp (encode-esp (data->byte-buffers ?decoded)))) => (data->byte-vec ?decoded)
     (encode-esp (decode-esp ?encoded)) => (unchecked-byte-vec ?encoded))
   ?encoded                 ?decoded
   rocker-switch-telegram   rocker-switch
   ok-response-telegram     ok-response))
