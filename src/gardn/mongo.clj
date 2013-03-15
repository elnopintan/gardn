(ns gardn.mongo
  (:require [gardn.core :as g]
            [monger.core :as m]
            [monger.collection :as mc])
  (:use monger.operators
        [monger.conversion :only [from-db-object]])
  (:import gardn.core.Store))


(defn- do-persist [entity]
  
  )

(deftype MongoStore [db]
  Store
  (find-entity [_ id] nil)
  (persist! [_ entity] nil)
  )

(comment 
(print-str (java.util.Date. 0))


(m/connect-via-uri! "mongodb://localhost/gardn")

(mc/ensure-index "gardn" { :reference 1 :seq 1 } { :unique true })

(mc/remove "gardn" {})
(mc/insert "gardn" {:reference "Nacho" :origin "origin" 
                    :seq 2 :value "5" })

(mc/find-maps "gardn" {})
 
)