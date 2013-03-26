(ns gardn.core)

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


(defn find-entity [store id]
  (read-string (find-entity-str store id)))

(defprotocol Store
  (find-entity-str [this id]
      "Retrieves a given entity by it's Id. 
   If instance is :last retrieva the last entity")
  (persist! [this entity]
   "Persists a given gardn entity. Returns true if it's persisted and false 
   if there is already an entity with the same origin."))

