# gardn

Key-value store of [edn](https://github.com/edn-format/edn) data with memory.

Gardn is a key-value store that maintains the history of the entities stored.
An entity can be queried by its reference (a user defined name) or by instance.
When an entity is updated, its previous state is not deleted. A new one is created. 
This new value provides a reference to it's last state, that can be queried.
A Gardn entity can store references to another gardn entities o instances.

## Usage

The first step is to create a Store, using any of the available implementations:

```clojure
  (ns example
  (:require [gardn.core :as g]
    [gardn.memory :as gm]))
  
  (def my-store (g/store (gm/memory-store)))
```
To create a new gardn entity, a reference-id and an initial value must be provided

```clojure
  
  (def new-entity (g/entity "myref" [1 2 3]))
  ; #gardn.io/data {:reference "myref" :origin nil 
  ;                 :instance {:hash-code 123 :seq-number 0} :value [1 2 3]}
  
```
The reference must be a String, and the value can be any edn object.
When created, a gardn entity receives an id based on its reference, value and sequence number.
Given an entity a new value can be created with the same reference. 
To do so, the id of a previous instance of the same entity must be given.

```clojure
  
  (def my-entity-2 (g/next-entity (:id my-entity) (conj (:value my-entity) 4)))
  ; #gardn.io/data {:reference "myref" :origin {:hash 123 :seq 0} 
  ;                 :instance {:hash-code 234 :seq-number 1} :value [1 2 3 4]}
  
```
The created entitys can be recorded into a store. But only if:

* Is a new entity and there is no entity stored with the same reference.
* Is the next instance of the last stored entity with the same entity.

```clojure
  
 (g/persist! my-store my-entity) 
 ; true
 (g/persist! my-store my-entity) 
 ; false
 (g/persist! my-store my-entity-2) 
 ; true
```
The entities can be retrieved from the store using its reference and instance id.

```clojure
  (g/get-entity my-store {:reference "myref" :instance {:hash-code 123 :seq 0}})
  ; #gardn.io/data {:reference "myref" :origin nil 
  ;                 :instance {:hash-code 123 :seq-number 0} :value [1 2 3]}
  
```
The last instance of a reference can be read using :last as instand id.

```clojure
  (g/get-entity my-store {:reference "myref" :instance :last})
  ; #gardn.io/data {:reference "myref" :origin {:hash 123 :seq 0} 
  ;                 :instance {:hash-code 234 :seq-number 1} :value [1 2 3 4]}
  
```
Gardn ids can be persisted:

```clojure
 (g/persist! my-store (g/entity "myotheref" {:count 1 :pointer (:id my-entity-2)})) 
  ; true
 (g/get-entity my-store {:reference "myotheref" :instance :last})
  ; #gardn.io/data {:reference "myotheref" origin nil :instance {:hash-code 444 :seq-number 0}
  ;                 :value {:count 1 
  ;                         :pointer #gardn.io/data {:reference "myref" 
  ;                                         :instance {:hash-code 123 :seq-number 0}}}}
  
```
A gardn store can be created with custom tagged literal readers for [edn](https://github.com/edn-format/edn) data.

```clojure
 (def my-custom-store (g/store (gm/memory-store) {'example/custom example/custom-reader}))
```
Currently there are two implementations of gardn stores.

A memory store

```clojure
  (ns example
  (:require [gardn.core :as g]
    [gardn.memory :as gm]))
  
  (def my-store (g/store (gm/memory-store)))
```
A mongodb store

```clojure

  (ns example
  (:require [gardn.core :as g]
    [gardn.mongo :as gmdb]))
  
  (def my-mongo-store (g/store (gmdb/mongo-store "localhost:27017")))
  
```
The mongo store can have multiple buckets (mongo collections) using a root mongo-store.

```clojure

  (def my-mongo-bucket (g/store (gmdb/mongo-bucket-store my-mongo-store "mybucket")))
  
```

## License

Distributed under the Eclipse Public License, the same as Clojure.
