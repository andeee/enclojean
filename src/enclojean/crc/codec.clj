(ns enclojean.crc.codec
  (:require [enclojean.crc.core :refer [calc-crc8]] 
            [gloss.core.protocols :refer [Reader Writer read-bytes
                                          write-bytes sizeof]]
            [gloss.data.bytes :refer [drop-bytes dup-bytes take-bytes]]
            [gloss.io :refer [contiguous to-buf-seq]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]))

(defn calc-crc8<-buf-seq [buf-seq]
  (-> (to-byte-array buf-seq) calc-crc8 unchecked-byte))

(defn read-crc8 [buf-seq]
  (-> (to-byte-array buf-seq) (aget 0)))

(defn byte-to-buf-seq [b]
  (-> (conj [] b) byte-array to-byte-buffers))

(defn crc8-frame [codec]
  (reify
    Reader
    (read-bytes [_ buf-seq]
      (let [len (sizeof codec)
            [_ x remainder] (read-bytes codec (take-bytes (dup-bytes buf-seq) len))
            expected-crc8 (calc-crc8<-buf-seq (take-bytes buf-seq len))
            decoded-crc8 (read-crc8 (take-bytes (drop-bytes buf-seq len) 1))
            success (= expected-crc8 decoded-crc8)]
        [success x (drop-bytes remainder 1)]))
    Writer
    (sizeof [_]
      (+ (sizeof codec) 1))
    (write-bytes [_ buf-seq v]
      (let [buf-seq (write-bytes codec buf-seq v)
            crc8-buf-seq (byte-to-buf-seq (calc-crc8<-buf-seq buf-seq))]
        (concat buf-seq crc8-buf-seq)))))
