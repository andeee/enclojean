(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]
            [gloss.core :refer [enum header byte-count]]))

(def command-code-frame 
  (enum :ubyte
        {:write-sleep 0x01
         :write-reset 0x02
         :read-version 0x03}))

(def common-command-frame
  (header [:command command-code-frame]
          {:write-reset []
           :write-sleep [:sleep-period :int32]
           :read-version []}
          :command))

(defmethod packet :common-command [a-map]
  (reify Packet
    (header->body [_ h] common-command-frame)
    (body->header [_ b] {:data-length 1
                         :optional-length 0})))
