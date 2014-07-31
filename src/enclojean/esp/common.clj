(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]
            [gloss.core :refer [enum header byte-count 
                                compile-frame defcodec]]
            [gloss.core.protocols :refer [sizeof]]))

(def command-code-frame 
  (enum :ubyte
        {:write-sleep 0x01
         :write-reset 0x02
         :read-version 0x03
         :read-sys-log 0x04
         :write-sys-log 0x05
         :write-built-in-self-test 0x06
         :write-id-base 0x07
         :read-id-base 0x08
         :write-repeater-level 0x09
         :read-repeater-level 0x0A
         :write-filter-add 0x0B
         :write-filter-delete 0x0C
         :write-filter-delete-all 0x0D
         :write-filter-enable 0x0E
         :read-filters 0x0F
         :write-wait-maturity 0x10
         :write-subtelegram 0x11
         :write-memory 0x12
         :read-memory 0x13
         :read-memory-address 0x14
         :read-security 0x15
         :write-security 0x16
         :write-learnmode 0x17
         :read-learnmode 0x18
         :write-secure-device-add 0x19
         :write-secure-device-delete 0x1A
         :read-secure-device-by-index 0x1B
         :write-mode 0x1C
         :read-number-of-secure-devices 0x1D
         :read-secure-device-by-id 0x1E
         :write-secure-device-add-psk 0x1F
         :write-secure-device-send-teachin 0x20
         :write-temporary-rolling-code-window 0x21
         :read-secure-device-psk 0x22}))

(defcodec write-sleep {:command :write-sleep :sleep-period :uint32})
(defcodec write-reset {:command :write-reset})
(defcodec read-version {:command :read-version})
(defcodec read-sys-log {:command :read-sys-log})
(defcodec write-sys-log {:command :write-sys-log})
(defcodec write-built-in-self-test {:command :write-built-in-self-test})
(defcodec write-id-base {:command :write-id-base :base-id :uint32})
(defcodec read-id-base {:command :read-id-base})

(defn get-command-codec [command-kw]
  (command-kw {:write-reset write-reset
               :write-sleep write-sleep
               :read-version read-version
               :read-sys-log read-sys-log
               :write-sys-log write-sys-log
               :write-built-in-self-test write-built-in-self-test
               :write-id-base write-id-base
               :read-id-base read-id-base}))

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
