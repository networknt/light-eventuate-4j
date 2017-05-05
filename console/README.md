# Light event sourcing admin consile

## Installation

cd app

npm install

npm start

### Admin console features:

-- display the entities and snapshot in the event store

-- for selected entity, display event list with detail

-- search by entity type, event type

-- get the entity/event detail by click the record from search result

--from then entity id from detail page, get Kafka topic information

---verify Kafka topic with entity/event information in mySql database

-- if the event doesn't published to Kafka for some reason, re-publish the event (admin user only)

--apply Kafka stream API (backend is not ready yet) for existing topic/message. It could be very useful for special symbol in reference data.

--- system health check


