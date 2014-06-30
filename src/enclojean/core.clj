(ns enclojean.core
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [gloss.core :refer [compile-frame defcodec
                                enum finite-block header
                                ordered-map]]))

(def sync-frame (enum :ubyte {:sync 0x55}))

(defn header->body [h] (crc8-frame (compile-frame (ordered-map :header h
                                                               :data (finite-block (:data-length h))
                                                               :optional-data (finite-block (:optional-length h))))))

(defn body->header [b] {:data-length (.capacity (:data b))
                        :optional-length (.capacity (:optional-data b))
                        :packet-type 0})

(defcodec esp3-frame [sync-frame
                      (crc8-frame (header (ordered-map :data-length     :uint16
                                                       :optional-length :ubyte
                                                       :packet-type     :ubyte)
                                          header->body
                                          body->header))])
