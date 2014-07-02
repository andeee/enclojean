(ns enclojean.test-utils
  (:require [enclojean.crc.core :refer [unchecked-byte-array]]
            [byte-streams :refer [to-byte-buffer]]))

(defn TV [byte-seq]
    (unchecked-byte-array byte-seq))

(defn TV->buf [byte-seq]
  (to-byte-buffer (TV byte-seq)))
