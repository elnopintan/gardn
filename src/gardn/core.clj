(ns gardn.core
  (:require [gardn.io :as io]
            [clojure.edn :as edn])
  (:import [gardn.io Data Id]))


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

(defprotocol Store
  (find-entity-str [this id]
      "Retrieves a given entity by it's Id. 
   If instance is :last retrieva the last entity")
  (persist! [this entity]
   "Persists a given gardn entity. Returns true if it's persisted and false 
   if there is already an entity with the same origin."))

(defn store 
  "Creates a store using one implementation"
  ([store-impl] (store store-impl {}))
  ([store-impl readers] (with-meta store-impl {:readers readers})))
  
(defn find-entity
  "Retreives an entity by its Id. 
  Uses edn reader to deserialize the data. 
  If provided a readers map will use the given function to read known tagged literals.
  Any non recognized tagged literal will be given as io/TaggedLiteral type"
  ([store id]
   (let [readers (get (meta store) :readers {})]
    (edn/read-string {:readers (merge {`io/data io/data-reader} readers)
                    :default io/tagged-literal-reader}
                   (find-entity-str store id)))))


