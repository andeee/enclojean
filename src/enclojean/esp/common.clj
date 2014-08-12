(ns enclojean.esp.common
  (:require [enclojean.esp.core :refer [packet Packet]]
            [gloss.core :refer [enum header ordered-map
                                compile-frame]]
            [gloss.core.protocols :refer [sizeof]]
            [clojure.set :refer [superset?]]))

(def on-off-frame
  (enum :byte {:off 0, :on 1}))

(def repeater-level-frame
  (enum :byte {:off 0, :level-1 1, :level-2 2}))

(def filter-type-frame
  (enum :byte {:device-id 0 :r-org 1, :dbm 2}))

(def filter-kind-frame
  (enum :ubyte {:blocks 0x00, :apply 0x80}))

(def and-or-frame
  (enum :byte {:or 0, :and 1}))

(def memory-type-frame
  (enum :byte {:flash 0x00, :ram-0 0x01, :data-ram 0x02, :idata-ram 0x03, :xdata-ram 0x04}))

(def memory-area-frame 
  (enum :byte {:config-area 0, :smart-ack-table 1, :system-error-log 2}))

(def direction-frame
  (enum :byte {:inbound-table 0x00, :outbound-table 0x01, :outbound-table-broadcast 0x02}) )

(def mode-frame
  (enum :byte {:compatible-mode 0x00, :advanced-mode 0x01}))

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
   :write-filter-delete [0x0C :filter-type filter-type-frame :filter-value :uint32]
   :write-filter-delete-all 0x0D
   :write-filter-enable [0x0E :filter-enable on-off-frame :filter-operator and-or-frame]
   :read-filters 0x0F
   :write-wait-maturity [0x10 :wait-end-maturity-enable on-off-frame]
   :write-subtelegram [0x11 :write-subtelegram-enable on-off-frame]
   :write-memory [0x12 :memory-type memory-type-frame :memory-address :uint32 :memory-data :ubyte]
   :read-memory [0x13 :memory-type memory-type-frame :memory-address :uint32 :data-length :uint16]
   :read-memory-address [0x14 :memory-area memory-area-frame]
   :read-security 0x15
   :write-security [0x16 :security-level :ubyte :key :uint32 :rolling-code :uint32]
   :write-learnmode [0x17 :learn-mode-enable on-off-frame :timeout :uint32 :optional [:channel :ubyte]]
   :read-learnmode 0x18
   :write-secure-device-add [0x19 :security-level-format :ubyte :device-id :uint32 :private-key (repeat 16 :ubyte) :rolling-code (repeat 3 :ubyte) :optional [:direction direction-frame]]
   :write-secure-device-delete [0x1A :device-id :uint32 :optional [:direction direction-frame]]
   :read-secure-device-by-index [0x1B :index :ubyte :optional [:direction direction-frame]]
   :write-mode [0x1C :mode mode-frame]
   :read-number-of-secure-devices [0x1D :optional [:direction direction-frame]]
   :read-secure-device-by-id [0x1E :device-id :uint32 :optional [:direction direction-frame]]
   :write-secure-device-add-psk [0x1F :device-id :uint32 :pre-shared-key (repeat 16 :ubyte)]
   :write-secure-device-send-teachin [0x20 :device-id :uint32 :optional [:teach-in-info :ubyte]]
   :write-temporary-rolling-code-window [0x21 :rolling-code-enable on-off-frame :rolling-code-window :uint32]
   :read-secure-device-psk [0x22 :device-id :uint32]})

(defn get-command-code [command] 
  (let [command-params (second command)]
    [(first command) (if (vector? command-params)
                       (first command-params)
                       command-params)]))

(def command-codes 
  (apply hash-map (apply concat (map get-command-code commands))))

(def command-code-frame
  (enum :ubyte command-codes))

(defn get-all-command-map [command-kw]
  (let [command-params (command-kw commands)]  
    (apply array-map
            (concat [:command command-kw]
                    (when (vector? command-params)
                      (rest command-params))))))

(defn get-command-map [command-kw]
  (-> (get-all-command-map command-kw)
      (dissoc :optional)))

(defn get-optional-map [command-kw]
  (apply array-map
         (:optional (get-all-command-map command-kw))))

(defn pre-to-codec [map-fn command-kw]
  (apply concat
         (seq (map-fn command-kw))))

(defn to-codec [map-fn command-kw]
  (compile-frame
   (apply ordered-map
          (pre-to-codec map-fn command-kw))))

(def get-command-codec
  (partial to-codec get-command-map))

(def get-optional-codec
  (partial to-codec get-optional-map))

(defn get-compound-codec [h command-kw]
  (compile-frame
   (apply ordered-map
          (concat
           (pre-to-codec get-command-map command-kw)
           (when (> (:optional-length h) 0)
             (pre-to-codec get-optional-map command-kw))))))

(defn get-common-command-frame [h]
  (header command-code-frame
          (partial get-compound-codec h)
          :command))

(defn optional-data-available? [b]
  (let [command-kw (:command b)
        optional-keys (set (keys (get-optional-map command-kw)))
        used-keys (set (keys b))]
    (superset? used-keys optional-keys)))

(defmethod packet :common-command [a-map]
  (reify Packet
    (header->body [_ h] (get-common-command-frame h))
    (body->header [_ b] {:data-length (+ (sizeof command-code-frame)
                                         (sizeof (get-command-codec (:command b))))
                         :optional-length (if (optional-data-available? b)
                                            (sizeof (get-optional-codec (:command b)))
                                            0)})))
