(ns gardn.memory 
  (:require [gardn.core :as g])
  (:import gardn.core.Store))

(defn- do-get [entities {:keys [refernce instance]}] 
  (entities id))

(defn- do-persist [entities {:keys [id origin] :as entity}] 
  (cond
   (nil? origin) (assoc entities id entity)
   (= (last entities) origin) (assoc entities id entity)
    :default entities))

(deftype MemoryStore [store]
  Store
  (get-entity [_ id]
       (do-get @store id))
  (persist! [_ entity]
            (= entity ((swap! store do-persist entity)
           (:id entity)))))


(defn new-memory-store []
  (MemoryStore. (atom (sorted-map-by 
                       (fn [{a :instance}  {b :instance}] (< a b))))))


(comment
(def my-memory (new-memory-store))
(.store my-memory)

(g/update-entity (g/new-entity "Nacho" 1) inc)
(g/persist! my-memory (g/update-entity (g/new-entity "Nacho" 1) inc))

(g/get-entity my-memory (:id (g/new-entity "Nacho" 1)))
  )