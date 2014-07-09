(ns enclojean.bytes
  (:import [javax.xml.bind DatatypeConverter])
  (:require [clojure.string :as str]))

(defn from-seq [seq]
  (byte-array (map unchecked-byte seq)))

(defn from-hex [hex-string]
  (DatatypeConverter/parseHexBinary (str/replace hex-string #"\s+" "")))

(defn to-seq [bytes]
  (seq bytes))

(defn unchecked-seq [bytes]
  (-> bytes from-seq to-seq))
