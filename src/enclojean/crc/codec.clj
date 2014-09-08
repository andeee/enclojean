(ns enclojean.crc.codec
  (:require [enclojean.crc.core :refer [calc-crc8]]
            [enclojean.bytes :as bytes]
            [gloss.core.protocols :refer [Reader Writer read-bytes
                                          write-bytes sizeof with-buffer]]
            [gloss.data.bytes :refer [drop-bytes dup-bytes take-bytes 
                                      byte-count duplicate]]
            [byte-streams :refer [to-byte-buffers to-byte-array]]))

(defn calc-crc8<-buf-seq [buf-seq]
  (-> (to-byte-array buf-seq) calc-crc8 unchecked-byte))

(defn byte-to-buf-seq [b]
  (-> (bytes/from-seq [b]) to-byte-buffers))

(defn calc-crc8<->buf-seq [buf-seq]
  (byte-to-buf-seq (calc-crc8<-buf-seq buf-seq)))

(defn calc-crc8<->buf [buf]
  (let [crc8 (-> (to-byte-array (.flip (duplicate buf))) calc-crc8 unchecked-byte)]
    (.put buf crc8)))

(defn read-crc8 [buf-seq]
  (when buf-seq
    (-> (to-byte-array buf-seq) (aget 0))))

(defn crc8-frame [codec]
  (reify
    Reader
    (read-bytes [this buf-seq]
      (let [len (or (sizeof this) (byte-count buf-seq))
            available (- len 1)
            [_ x _] (read-bytes codec (take-bytes (dup-bytes buf-seq) available))
            expected-crc8 (calc-crc8<-buf-seq (take-bytes buf-seq available))
            decoded-crc8 (read-crc8 (take-bytes (drop-bytes buf-seq available) 1))
            success (= expected-crc8 decoded-crc8)]
        [success x (drop-bytes buf-seq len)]))
    Writer
    (sizeof [_]
      (if (sizeof codec)
        (+ (sizeof codec) 1)
        nil))
    (write-bytes [this buf v]
      (if (sizeof this)
        (with-buffer [buf (sizeof this)]
          (prn v)
          (write-bytes codec buf v)
          (calc-crc8<->buf buf))
        (let [buf-seq (write-bytes codec buf v)]
          (concat buf-seq (calc-crc8<->buf-seq buf-seq)))))))
