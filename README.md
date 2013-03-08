# gardn

Key value store of EDN data with memory. 

## Usage

Gardn is a Key value store that maintains the history of the entities stored.
An entity can be queried by its reference (a user defined name) o by instance.
When an entity is updated, its previous state is not deleted. A new one is created. 
This new value provides a reference to it's last state, that can be queried.

## License

Distributed under the Eclipse Public License, the same as Clojure.
