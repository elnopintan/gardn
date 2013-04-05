
(ns gardn.mongo
  (:require [gardn.core :as g]
            [somnium.congomongo :as m]
            [clojure.edn :as edn])
  (:import [gardn.core Store Data Id]))

(defn id-query-map [{:keys [reference] 
                          {:keys [seq-number hash-code]} :instance}]
  {:reference reference 
   :seq-number seq-number
   :hash-code hash-code})

(defn do-find-entity [{:keys [reference instance] :as id} conn collection]
  (cond 
   (= instance :last)
     (m/with-mongo conn 
       (first (m/fetch  collection 
                     :where {:reference reference}
                     :sort {:seq-number -1})))
   true (let [{:keys [seq-number hash-code]} instance]
              (m/with-mongo 
                 conn  (m/fetch-one
                         collection
                         :where (id-query-map id))))))


(defn return-entity [ entity conn collection] 
  (some-> entity
          (do-find-entity conn collection)
          :value 
          ))

(defn can-persist? [{:keys [origin]} conn collection]
  (letfn  [(exist-origin? [id]
                        (= 
                         (m/with-mongo 
                          conn (m/fetch-count collection
                                   :where (id-query-map id))) 1))]
    (or (nil? origin)
      (exist-origin? origin))))

(defn do-persist! [{:keys [id] :as entity} conn collection]
    (when (can-persist? entity conn collection)
      (try
        (m/with-mongo conn
          (m/insert! collection
                     (assoc (id-query-map id) :value (pr-str entity))))
        true
        (catch com.mongodb.MongoException m
          ; 11000 code says there is already an entity with the same reference and seq-number
          (if (= (.getCode m) 11000)
            nil
            (throw m))))))


(defrecord MongoStore [conn collection]
  Store
  (find-entity-str [_ id] (return-entity id conn collection))
  (persist! [_ entity] (not (nil? (do-persist! entity conn collection)))))

(defn- init-store [conn collection]
    (m/with-mongo 
     conn 
     (m/add-index! collection [:reference [:seq-number -1]] :unique true ))
  (MongoStore. conn collection))

(defn mongo-store [connection-uri]
  (let [conn (m/make-connection connection-uri)]
    (m/set-write-concern conn :journaled)
     (init-store conn :gardn)))

(defn mongo-bucket-store [root-store bucket]
  (init-store (.conn root-store) bucket))
  

(comment
(def mystore (g/store (mongo-store "mongodb://localhost:27017/gardn")))
(def mystore (g/store (mongo-bucket-store mystore "nacho")))
(def myentity (g/entity "number" 1))


(g/persist! mystore myentity)
(:id myentity)
(g/find-entity mystore {:reference "number" :instance :last})

(let [{:keys [id value]} 
      (g/find-entity mystore {:reference "number" :instance :last})]
  (g/persist! mystore (g/next-entity id (inc value))))
  
  )
 
