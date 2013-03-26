(ns gardn.mongo
  (:require [gardn.core :as g]
            [monger.core :as m]
            [monger.collection :as mc]
            [monger.query :as mq]
            [clojure.edn :as edn])
  (:use monger.operators
        [monger.conversion :only [from-db-object]])
  (:import [gardn.core Store Data Id]))

(defn id-query-map [{:keys [reference] 
                          {:keys [seq-number hash-code]} :instance}]
  {:reference reference 
   :seq-number seq-number
   :hash-code hash-code})

(defn do-find-entity [{:keys [reference instance] :as id}]
  (cond 
   (= instance :last) 
     (first (mq/with-collection "gardn" 
                                (mq/find {:reference reference})
                                (mq/sort (sorted-map :seq-number -1))))
   :default (let [{:keys [seq-number hash-code]} instance]
              (first (mq/with-collection "gardn" 
                                         (mq/find (id-query-map id)))))))


(defn return-entity [entity] 
  (some-> entity
          do-find-entity
          :value 
          ))

(defn can-persist? [{:keys [origin]}]
  (letfn  [(exist-origin? [id]
                        (= (mc/count "gardn"
                                     (id-query-map id)) 1))]
    (or (nil? origin)
      (exist-origin? origin))))

(defn do-persist! [{:keys [id] :as entity}]
    (when (can-persist? entity)
      (try 
        (mc/insert "gardn"
                   (assoc (id-query-map id) :value (pr-str entity)))
        true
        (catch com.mongodb.MongoException m
          ; 11000 code says there is already an entity with the same reference and seq-number
          (if (= (.getCode m) 11000)
            nil
            (throw m))))))


(deftype MongoStore [db]
  Store
  (find-entity-str [_ id] (return-entity id))
  (persist! [_ entity] (not (nil? (do-persist! entity)))))

(defn mongo-store [connection-uri]
  (m/connect-via-uri! connection-uri)
  (mc/ensure-index "gardn" { :reference 1 :seq-number -1 } { :unique true })
  (MongoStore. connection-uri))


  

(comment 
(def mystore (mongo-store "mongodb://localhost/gardn"))

(def myentity (g/entity "number" 1))

(g/persist! mystore myentity)
(:id myentity)
(g/find-entity mystore {:reference "number" :instance :last})


(let [{:keys [id value]} 
      (g/find-entity mystore {:reference "number" :instance :last})]
  (g/persist! mystore (g/next-entity id (inc value))))

)
 
