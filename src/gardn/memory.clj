(ns gardn.memory 
  (:require [gardn.core :as g])
  (:import gardn.core.Store))

(def instances-map
  (sorted-map-by 
         (fn [{a :seq-number} {b :seq-number} ] (< a b))))

(defn- do-get [entities {:keys [reference instance]}]
  (if-let [instance-map (entities reference)]
   (cond (= :last instance) (last (last instance-map))
         :default (instance-map instance))))
  

(defn- new-reference? [entities {:keys [origin] {:keys [reference]} :id}]
  (and (nil? origin) (not (entities reference))))

(defn- origin-last? [entities {{:keys [reference instance]} :origin}]
  (= (first (last (entities reference))) instance))


(defn- do-persist [entities {{:keys [reference instance]} :id :as entity}]
  (let [entity-str (pr-str entity)
        new-reference (new-reference? entities entity)]
    (cond-> entities
     new-reference (assoc reference instances-map)
     (or new-reference 
         (origin-last? entities entity)) (assoc-in [reference instance] entity-str))))


(deftype MemoryStore [store]
  Store
  (find-entity-str [_ id]
       (do-get @store id))
  (persist! [_ entity]
            (= (pr-str entity) 
               (do-get 
                       (swap! store do-persist entity)
           (:id entity)))))

(defn memory-store []
  (MemoryStore. (atom {})))

(comment
(def mystore (memory-store))

(def myentity (g/entity "number" 1))
myentity

(g/persist! mystore myentity)

(g/find-entity mystore {:reference "number" :instance :last})


(let [{:keys [id value]} 
      (g/find-entity mystore {:reference "number" :instance :last})]
  (g/persist! mystore (g/next-entity id (inc value))))

 )