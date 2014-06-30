(ns enclojean.core
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [gloss.core :refer [compile-frame defcodec
                                enum finite-block header
                                ordered-map]]))

(def sync-frame (enum :ubyte {:sync 0x55}))

(def packet-type-frame
  (enum :ubyte
        {:radio 0x01
         :response 0x02
         :radio-sub-telegram 0x03
         :event 0x04
         :common-command 0x05
         :smart-ack-command 0x06
         :remote-mangement-command 0x07
         :radio-message 0x09
         :radio-advanced 0x10}))

(defn header->body [h]
  (crc8-frame
   (compile-frame
    (ordered-map :packet-type (:packet-type h)
                 :data (finite-block (:data-length h))
                 :optional-data (finite-block (:optional-length h))))))

(defn body->header [b]
  {:data-length (.capacity (:data b))
   :optional-length (.capacity (:optional-data b))
   :packet-type (:packet-type b)})

(defcodec esp3-frame
  [sync-frame
   (header
    (crc8-frame
     (ordered-map :data-length     :uint16
                  :optional-length :ubyte
                  :packet-type     packet-type-frame))
    header->body
    body->header)])
