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

(defn do-find-entity [{:keys [reference instance] :as id} conn]
  (cond 
   (= instance :last)
     (m/with-mongo conn 
       (first (m/fetch  :gardn 
                     :where {:reference reference}
                     :sort {:seq-number -1})))
   true (let [{:keys [seq-number hash-code]} instance]
              (m/with-mongo 
                 conn  (m/fetch-one
                         :gardn 
                         :where (id-query-map id))))))


(defn return-entity [ entity conn] 
  (some-> entity
          (do-find-entity conn)
          :value 
          ))

(defn can-persist? [{:keys [origin]} conn]
  (letfn  [(exist-origin? [id]
                        (= 
                         (m/with-mongo 
                          conn (m/fetch-count :gardn
                                   :where (id-query-map id))) 1))]
    (or (nil? origin)
      (exist-origin? origin))))

(defn do-persist! [{:keys [id] :as entity} conn]
    (when (can-persist? entity conn)
      (try
        (m/with-mongo conn
          (m/insert! :gardn
                     (assoc (id-query-map id) :value (pr-str entity))))
        true
        (catch com.mongodb.MongoException m
          ; 11000 code says there is already an entity with the same reference and seq-number
          (if (= (.getCode m) 11000)
            nil
            (throw m))))))


(deftype MongoStore [conn]
  Store
  (find-entity-str [_ id] (return-entity id conn))
  (persist! [_ entity] (not (nil? (do-persist! entity conn)))))

(defn mongo-store [connection-uri]
  (let [conn (m/make-connection connection-uri)]
    (m/set-write-concern conn :journaled)
    (m/with-mongo 
     conn 
     (m/add-index! :gardn [:reference [:seq-number -1]] :unique true ))
  (MongoStore. conn)))


  

(comment
(def mystore (mongo-store "mongodb://localhost:27017/gardn"))

(def myentity (g/entity "number" 1))

(g/persist! mystore myentity)
(:id myentity)
(g/find-entity mystore {:reference "number" :instance :last})


(let [{:keys [id value]} 
      (g/find-entity mystore {:reference "number" :instance :last})]
  (g/persist! mystore (g/next-entity id (inc value))))

)
 
