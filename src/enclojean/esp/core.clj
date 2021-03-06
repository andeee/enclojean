(ns enclojean.esp.core
  (:require [enclojean.crc.codec :refer [crc8-frame]]
            [gloss.core :refer [byte-count compile-frame
                                defcodec enum header
                                ordered-map finite-block]]
            [gloss.core.structure :refer [convert-sequence]]))

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
    (header->body [_ h] (ordered-map
                         :data (finite-block (:data-length h))
                         :optional-data (finite-block (:optional-length h))))
    (body->header [_ b] (array-map
                         :data-length (byte-count (:data b))
                         :optional-length (byte-count (:optional-data b))))))

(defn split-packet-body [body]
  (map #(apply hash-map %)
       (let [packet-type (first body)
             packet-body (rest body)
             more? (> (count packet-body) 1)
             packet-body-fn (if more? concat identity)]
         [packet-type (apply packet-body-fn packet-body)])))

(defn join-packet-body [packet-type-and-body]
  (apply array-map (apply concat (apply concat packet-type-and-body))))

(defn header->packet-body [h]
  (let [p (packet h)]
    (crc8-frame
     (compile-frame
      [{:packet-type (:packet-type h)}
       (header->body p h)]
      split-packet-body
      join-packet-body))))

(defn packet-body->header [b]
  (let [p (packet b)]
    (conj {:packet-type (:packet-type b)}
          (body->header p b))))

(defn prepend-sync [values]
  (conj [:sync] values))

(defn remove-sync [values]
  (apply identity (rest values)))

(defcodec esp
  (compile-frame
   [sync-frame
    (header
     (crc8-frame
      (ordered-map :data-length     :uint16
                   :optional-length :ubyte
                   :packet-type     packet-type-frame))
     header->packet-body
     packet-body->header)]
   prepend-sync
   remove-sync))
