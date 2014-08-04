(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]
            [gloss.core :refer [enum header ordered-map
                                compile-frame]]
            [gloss.core.protocols :refer [sizeof]]))

(def on-off-frame
  (enum :byte {:off 0, :on 1}))

(def repeater-level-frame
  (enum :byte {:off 0, :level-1 1, :level-2 2}))

(def filter-type-frame
  (enum :byte {:device-id 0 :r-org 1, :dbm 2}))

(def filter-kind-frame
  (enum :ubyte {:blocks 0x00, :apply 0x80}))

(def commands
  {:write-sleep [0x01 :sleep-period :uint32] 
   :write-reset 0x02
   :read-version 0x03
   :read-sys-log 0x04
   :write-sys-log 0x05
   :write-built-in-self-test 0x06
   :write-id-base [0x07 :base-id :uint32]
   :read-id-base 0x08
   :write-repeater-level [0x09 :repeater-enable on-off-frame :repeater-level repeater-level-frame]
   :read-repeater-level 0x0A
   :write-filter-add [0x0B :filter-type filter-type-frame :filter-value :uint32 :filter-kind filter-kind-frame]
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
   :read-secure-device-psk 0x22})

(defn get-command-code [command] 
  (let [command-params (second command)]
    [(first command) (if (vector? command-params)
                       (first command-params)
                       command-params)]))

(def command-codes 
  (apply hash-map (apply concat (map get-command-code commands))))

(def command-code-frame
  (enum :ubyte command-codes))

(defn get-command-codec [command-kw]
  (let [command-params (command-kw commands)]
    (compile-frame
     (apply ordered-map 
            (concat [:command command-kw]
                    (when (vector? command-params)
                      (rest command-params)))))))

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
