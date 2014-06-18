(ns enclojean.core
  (:use [gloss.core]
        [gloss.io]))

(def sync-frame (enum :ubyte {:sync 0x55}))

(defn header->body [h] (compile-frame (ordered-map :header h
                                                   :data (finite-block (:data-length h))
                                                   :optional-data (finite-block (:optional-length h))
                                                   :crc-data :ubyte)))

(defn body->header [b] {:data-length (.capacity (:data b))
                        :optional-length (.capacity (:optional-data b))
                        :packet-type 0
                        :crc-header 0})

(defcodec esp3-frame [sync-frame
   (header (ordered-map :data-length     :uint16
                        :optional-length :ubyte
                        :packet-type     :ubyte
                        :crc-header      :ubyte)
           header->body
           body->header)])
