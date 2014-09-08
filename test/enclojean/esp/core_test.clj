(ns enclojean.esp.core-test
  (:use midje.sweet)
  (:require [enclojean.esp.core :refer [esp]]
            [enclojean.bytes :as bytes]
            [gloss.io :refer [decode encode]]
            [byte-streams :refer [to-byte-array to-byte-buffers]]))

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

(def buffer->byte-vec (comp vec to-byte-array))

(def sample->byte-vec (comp vec bytes/from-seq))

(defn transform-data [decoded f]
  (reduce (fn [result data]
            (let [[key val] data]
              (if val (assoc result key (f val))
                  result)))
          decoded
          (select-keys decoded [:data :optional-data])))

(defn byte-buffers->data [decoded]
  (transform-data decoded buffer->byte-vec))

(defn data->byte-buffers [decoded]
  (transform-data decoded (comp to-byte-buffers bytes/from-seq)))

(defn data->byte-vec [decoded]
  (transform-data decoded sample->byte-vec))

(facts "about esp codec"
  (tabular
   (fact "roundtrip encodes/decodes telegrams"
     (byte-buffers->data (decode esp (encode esp (data->byte-buffers ?decoded)))) => (data->byte-vec ?decoded)
     (buffer->byte-vec (encode esp (decode esp (bytes/from-seq ?encoded)))) =>
     (sample->byte-vec ?encoded))
   ?encoded                 ?decoded
   rocker-switch-telegram   rocker-switch
   ok-response-telegram     ok-response))
