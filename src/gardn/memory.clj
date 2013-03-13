(ns gardn.memory 
  (:require [gardn.core :as g])
  (:import gardn.core.Store))

(defn- instances-map [{{:keys [instance]} :id :as entity}]
  (sorted-map-by 
         (fn [{a :seq} {b :seq} ] (< a b))
   instance entity))

(defn- do-get [entities {:keys [reference instance]}]
  (if-let [instance-map (entities reference)]
  (instance-map instance)))
  

(defn- do-persist [entities {:keys [id origin] :as entity}]
  (let [{:keys [reference instance]} id]
    (cond
     (and (nil? origin) (not (entities reference))) (assoc entities reference (instances-map entity))
     (= (first (last (entities reference))) (:instance origin)) (assoc-in entities [reference instance] entity)
    :default entities)))

(deftype MemoryStore [store]
  Store
  (get-entity [_ id]
       (do-get @store id))
  (persist! [_ entity]
            (= entity (do-get (swap! store do-persist entity)
           (:id entity)))))



(defn new-memory-store []
  (MemoryStore. (atom {})))



(def my-memory (new-memory-store))
(.store my-memory)

(g/persist! my-memory (g/new-entity "Nacho" 1))
(g/persist! my-memory (g/update-entity (g/new-entity "Nacho" 1) dec))

(g/get-entity my-memory (:id (g/update-entity (g/new-entity "Nacho" 1) inc)))
  