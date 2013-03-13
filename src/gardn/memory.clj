(ns gardn.memory 
  (:require [gardn.core :as g])
  (:import gardn.core.Store))

(defn- instances-map []
  (sorted-map-by 
         (fn [{{a :seq} :instance}  {{b :seq} :instance}] (< a b)))
  )

(defn- do-get [entities {:keys [reference instance]}]
  (if-let [instance-map (entities reference)]
  (instance-map instance)))
  

(defn- do-persist [entities {:keys [id origin] :as entity}]
  (let [{:keys [reference instance]} id]
    (cond
     (or (nil? origin) (empty? entities)) (assoc-in entities [reference instance] entity)
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
(last (@(.store my-memory) "Nacho"))

(g/persist! my-memory (g/new-entity "Nacho" 1))
(g/persist! my-memory (g/update-entity (g/new-entity "Nacho" 1) inc))

(g/get-entity my-memory (:id (g/new-entity "Nacho" 1)))
  