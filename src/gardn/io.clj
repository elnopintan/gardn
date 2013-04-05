(ns gardn.io)

;Gardn Id. 
;reference identifies a gardn entity.
;instance refer to a given state of the entity.
(defrecord Id 
  [reference instance])

;Gardn data. Represent a snapshot of a gardn entity. 
;origin is the Id of the previous state of the entity.
(defrecord Data 
  [origin id value])

(defmethod print-method Id [{:keys [reference instance]} w]
  (.write w "#gardn.io/data ")
  (print-method {:reference reference :instance instance} w))

(defmethod print-method Data [{:keys [origin value] 
                               {:keys [reference instance]} :id} w]
  (.write w "#gardn.io/data ")
  (print-method {:reference reference 
                 :instance instance
                 :value value
                 :origin (if origin (:instance origin) nil)} w))

(defn data-reader
  "Reads gardn data literals"
  [{:keys [reference origin instance value]}]
  (cond value (Data. (if origin (Id. reference origin) nil)
                (Id. reference instance)
                value)
        true (Id. reference id)))

;Gardn data for tagged literals
(deftype TaggedLiteral 
  [tag value])


(defmethod print-method TaggedLiteral [ d w]
  (.write w (str "#" (.tag d) " "))
  (print-method (.value d) w))

(defn tagged-literal-reader [t v] (TaggedLiteral. t v))