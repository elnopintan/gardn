(ns gardn.memory 
  (:require [gardn.core :as g])
  (:import gardn.core.Store))

(defn- instances-map [{{:keys [instance]} :id :as entity}]
  (sorted-map-by 
         (fn [{a :seq} {b :seq} ] (< a b))
   instance entity))

(defn- do-get [entities {:keys [reference instance]}]
  (if-let [instance-map (entities reference)]
    (cond (= :last instance) (last (last instance-map))
          :default (instance-map instance))))
  

(defn- do-persist [entities {:keys [id origin] :as entity}]
  (let [{:keys [reference instance]} id]
    (cond
     (and (nil? origin) (not (entities reference))) (assoc entities reference (instances-map entity))
     (= (first (last (entities reference))) (:instance origin)) (assoc-in entities [reference instance] entity)
    :default entities)))

(deftype MemoryStore [store]
  Store
  (find-entity [_ id]
       (do-get @store id))
  (persist! [_ entity]
            (= entity (do-get (swap! store do-persist entity)
           (:id entity)))))


(defn memory-store []
  (MemoryStore. (atom {})))

(comment

 (def my-memory (new-memory-store))
(g/persist! my-memory (g/entity "Nacho" 1))
(g/persist! my-memory (g/next-entity (:id (g/new-entity "Nacho" 1)) 2))

(g/find-entity my-memory (:id (g/next-entity (:id (g/new-entity "Nacho" 1)) inc)))
(g/persist! my-memory 
            (g/next-entity (:id (g/find-entity my-memory {:reference "Nacho" :instance :last})) 
            5))
 )
