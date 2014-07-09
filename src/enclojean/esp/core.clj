(ns enclojean.esp.core
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [gloss.core :refer [byte-count compile-frame 
                                defcodec enum finite-block
                                header ordered-map]]))

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

(defprotocol Packet
  (header->body [this h])
  (body->header [this b]))

(defmulti packet :packet-type)

(defmethod packet :default [a-map]
  (reify Packet
    (header->body [_ h] [:data (finite-block (:data-length h))
                         :optional-data (finite-block (:optional-length h))])
    (body->header [_ b] {:data-length (byte-count (:data b))
                         :optional-length (byte-count (:optional-data b))})))

(defn header->packet-body [h]
  (let [p (packet h)]
    (crc8-frame
     (compile-frame
      (apply ordered-map 
             (concat [:packet-type (:packet-type h)]
                     (header->body p h)))))))

(defn packet-body->header [b]
  (let [p (packet b)]
    (conj {:packet-type (:packet-type b)}
          (body->header p b))))

(defcodec esp
  [sync-frame
   (header
    (crc8-frame
     (ordered-map :data-length     :uint16
                  :optional-length :ubyte
                  :packet-type     packet-type-frame))
    header->packet-body
    packet-body->header)])
