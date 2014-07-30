(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]
            [gloss.core :refer [enum header byte-count 
                                compile-frame defcodec]]
            [gloss.core.protocols :refer [sizeof]]))

(def command-code-frame 
  (enum :ubyte
        {:write-sleep 0x01
         :write-reset 0x02
         :read-version 0x03}))

(defcodec write-sleep {:command :write-sleep :sleep-period :int32})
(defcodec write-reset {:command :write-reset})
(defcodec read-version {:command :read-version})

(defn get-command-codec [command-kw]
  (command-kw {:write-reset write-reset
               :write-sleep write-sleep
               :read-version read-version}))

(def common-command-frame
  (header command-code-frame
          get-command-codec
          :command))

(defmethod packet :common-command [a-map]
  (reify Packet
    (header->body [_ h] common-command-frame)
    (body->header [_ b] {:data-length (+ (sizeof command-code-frame)
                                         (sizeof (get-command-codec (:command b))))
                         :optional-length 0})))
