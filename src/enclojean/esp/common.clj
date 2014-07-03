(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]))

(defmethod packet :common-command [a-map]
  (reify Packet
    (header->body [_ h] [:command :byte])
    (body->header [_ b] {:data-length 1
                         :optional-length 0})))
