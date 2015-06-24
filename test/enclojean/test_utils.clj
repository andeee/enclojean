(ns enclojean.test-utils
  (:require [enclojean.bytes :as bytes] 
            [enclojean.esp.core :refer [esp]]
            [enclojean.crc.codec :refer [crc8-frame]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]
            [gloss.core :refer [compile-frame]]
            [gloss.io :refer [decode encode]]))

(def unchecked-byte-vec (comp vec bytes/from-seq))

(def unchecked-byte-buffers (comp to-byte-buffers bytes/from-seq))

(def to-byte-seq (comp bytes/to-seq to-byte-array))

(defn encode-esp [decoded]
  (-> (encode esp decoded) to-byte-seq))

(defn decode-esp [telegram]
  (decode esp (unchecked-byte-buffers telegram)))

(defn encode-crc8 [codec v]
  (-> (compile-frame codec)
      crc8-frame
      (encode v)
      to-byte-seq))

(defn decode-crc8 [codec x]
  (-> (compile-frame codec)
      crc8-frame
      (decode (unchecked-byte-buffers x))))
