(ns enclojean.test-utils
  (:require [enclojean.crc.core :refer [unchecked-byte-array]]))

(defn TV [byte-seq]
    (unchecked-byte-array byte-seq))
