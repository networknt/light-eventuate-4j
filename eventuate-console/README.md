# Light event sourcing admin consile

### Usage
- Clone or fork this repository
- Make sure you have [node.js](https://nodejs.org/) installed（>= 6.0）
- run `npm install -g webpack webpack-dev-server typescript` to install global dependencies
- run `npm install` to install dependencies
- run `npm start` to fire up dev server
- open browser to [`http://localhost:3000`](http://localhost:3000)




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


