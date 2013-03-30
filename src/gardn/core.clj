(ns gardn.core
  (:require ;[gardn.io :as io]
            [clojure.edn :as edn]))

;Gardn Id. 
;reference identifies a gardn entity.
;instance refer to a given state of the entity.
(defrecord Id 
  [reference instance])

;Gardn data. Represent a snapshot of a gardn entity. 
;origin is the Id of the previous state of the entity.
(defrecord Data 
  [origin id value])

(defn entity 
  "Creates a new entity with a given reference and value"
  [reference value]
  (Data. nil (Id. reference {:seq-number 0 :hash-code (.hashCode value)}) value))


(defn next-entity 
  "Updates an entity to a new value which is created applying a given function"
  [origin value]
  (let [{:keys [reference instance]} origin]
    (Data. origin (Id. reference 
                   {:seq-number (inc (:seq-number instance)) 
                    :hash-code (.hashCode value)}) 
           value)))

(defn data-reader [{:keys [reference origin id value]}]
  (cond value (Data. (if origin (Id. reference origin) nil)
                (Id. reference id)
                value)
        true (Id. reference id)))
 
(defmethod print-method Id [{:keys [reference instance]} w]
  (.write w "#gardn.core/data")
  (print-method {:reference reference :id instance} w))

(defmethod print-method Data [{:keys [origin value] 
                               {:keys [reference instance]} :id} w]
  (.write w "#gardn.core/data")
  (print-method {:reference reference 
                 :id instance
                 :value value
                 :origin (if origin (:instance origin) nil)} w))

(defn find-entity [store id]
  (edn/read-string {:readers {`data data-reader}} (find-entity-str store id) ))

(defprotocol Store
  (find-entity-str [this id]
      "Retrieves a given entity by it's Id. 
   If instance is :last retrieva the last entity")
  (persist! [this entity]
   "Persists a given gardn entity. Returns true if it's persisted and false 
   if there is already an entity with the same origin."))


