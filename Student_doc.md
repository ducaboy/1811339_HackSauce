# SYSTEM DESCRIPTION

HackSauce is a distributed automation platform capable of automatically gathering information about your Mars habitat conditions and changing the state of actuators in order to avoid system failures and severe thermodynamic consequences.
The system ingests sensor data in real time, applies automation rules, and allows the habitat operator to monitor live sensor readings, track actuator states, and manage automation rules from a single dashboard.

# USER STORIES:

1) As the habitat operator, I want to see the latest value of each sensor in real time so I can monitor habitat conditions.
2) As the habitat operator, I want to see the current state of all actuators so I know what is active.
3) As the habitat operator, I want the system to modify the state of actuators if the state conditions go out of normal ranges.
4) As the habitat operator, I want to see when an actuator was last updated so I know whether the state is recent.
5) As the habitat operator, I want to toggle and untoggle actuators so that i can regulate habitat conditions also manually.
6) As the habitat operator, I want to create automation rules so that the system can react automatically to sensor changes.
7) As the habitat operator, I want to update rules in real time so that i can regulate the habit conditions to my liking.
8) As the habitat operator, I want to toggle and untoggle rules in real time so that i can regulate the habitat conditions to my liking.
9) As the habitat operator, I want the rules to be persistent so that they can survive system failures and restarts.
10) As the habitat operator, I want to receive visual alerts when sensor values exceed critical thresholds so that I can react quickly.

# STANDARD INTERNAL EVENT FORMAT 

{<br>
  "type": "object",<br>
  "required": ["sensor_id", "captured_at", "measurements", "status"],<br>
  "properties": {<br>
    "sensor_id": { "type": "string" },<br>
    "captured_at": { "type": "string", "format": "date-time" },<br>
    "measurements": {<br>
      "type": "array",<br>
      "items": {<br>
        "type": "object",<br>
        "required": ["metric", "value", "unit"],<br>
        "properties": {<br>
          "metric": { "type": "string" },<br>
          "value": { "type": "number" },<br>
          "unit": { "type": "string" }<br>
        }<br>
      }<br>
    },<br>
    "status": { "type": "string", "enum": ["ok", "warning"] }<br>
  }<br>
}<br>

# CONTAINERS:

## CONTAINER_NAME: processing-service

### DESCRIPTION:
Implements a JMS Consumer, mantains in-memory-caching of the sensors' state, initializes and handles a Database with persistent automation rules, triggers actuators and exposes a REST API and SSE to the frontend

### USER STORIES

1) As the habitat operator, I want to see the latest value of each sensor in real time so I can monitor habitat conditions.
2) As the habitat operator, I want to see the current state of all actuators so I know what is active.
3) As the habitat operator, I want the system to modify the state of actuators if the state conditions go out of normal ranges.
4) As the habitat operator, I want to see a timestamp of the last actuator state update so I know how fresh the data is
5) As the habitat operator, I want to toggle and untoggle actuators so that i can regulate habitat conditions also manually.
6) As the habitat operator, I want to implement automatation rules so that the system can react automatically to sensor changes.
7) As the habitat operator, I want to update rules in real time so that i can regulate the habit conditions to my liking.
8) As the habitat operator, I want to toggle and untoggle rules in real time so that i can regulate the habitat conditions to my liking.
9) As the habitat operator, I want the rules to be persistent so that they can survive system failures and restarts.
10) As the habitat operator, I want to see a visual alert when a sensor exceeds a threshold.

### PORTS
8081:8081

### DESCRIPTION
The processing-service is the core backend of the platform. It acts as JMS Consumer to fetch normalized data from an Active MQ broker, updates in-memory cache of the sensors' state with the latest readings. On new incoming messages it loads the rules from the PostgreSQL database and evaluates each one. When a condition is a POST request is sent to the original server to change the state of the actuators. It also exposes a REST API, which working together with SSE, is able to provide data about actuators and sensors in real-time to the frontend. It also allows to perform CRUD operations on the rules stored, persistantly, in the database.

### PERSISTANCE EVALUATION
The processing-service requires persistent storage of the automation rules. Every time new measurements arrive the rules are loaded from the database and evaluated. A system of in-memory caching is also implemented storing the last value of the sensors' state, but not tracking their history.

### EXTERNAL SERVICES CONNECTION
The processing-service connects to an ActiveMQ broker via port 61616 through a message consumer, to PostgreSQL via port 5432 and the Mars simulator via port 8080.

### MICROSERVICES:

#### MICROSERVICE: jms-consumer
- TYPE: backend
- DESCRIPTION: Listens to the sensor.events queue on ActiveMQ. On every incoming message it deserializes the JSON payload into a SensorEvent object, updates the in-memory sensor cache with the latest reading for that sensor, triggers the rule engine to evaluate all enabled rules against the new event, and pushes an SSE notification to all connected frontend clients.
- PORTS: None (internal only)
- TECHNOLOGICAL SPECIFICATION:
Developed in Java using Spring Boot. Uses the @JmsListener annotation to subscribe to the sensor.events queue. Uses Jackson ObjectMapper for JSON deserialization. Handles both BytesMessage and TextMessage JMS message types for broker compatibility.
- SERVICE ARCHITECTURE:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
Key components of the stack:
Spring Boot:
Several Spring Boot starter dependencies are included:
spring-boot-starter-web: This is used for building web applications, including RESTful APIs.
spring-boot-starter-test: This is included for unit and integration testing purposes.
spring-boot-starter-activemq: This is to establish an ActiveMq connection.
com-fasterxml-jackson-core: This is used to speed up the mapping of the json message to a Java object.
The build process is managed by Apache Maven.
The service is realized with a single SensorConsumer class annotated with @Component. It holds references to the SensorStateCache, RuleEngine, and SseEmitterService and coordinates the full processing pipeline on each incoming message.

#### MICROSERVICE: rule-engine
- TYPE: backend
- DESCRIPTION: Evaluates rules against the incoming sensor event data and triggers the actuators' state change if the conditions are met. On each evaluation it loads the rules from the PostgreSQL database by using an interface, which maps the rules table to a Java object. If a condition is met a REST Post call on the simulator is made to update the state of the actuator.
PORTS: None (internal only)
- TECHNOLOGICAL SPECIFICATION:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
Uses Spring Data JPA to load rules from PostgreSQL. Uses RestTemplate to send actuator commands to the simulator REST API. Actuator states are cached in memory using a ConcurrentHashMap. The ActuatorClient initializes all 4 actuator states to OFF on startup via @PostConstruct.
The build process is managed by Apache Maven.
- SERVICE ARCHITECTURE:
The service is realized with a RuleEngine class that contains the condition evaluation logic and an ActuatorClient class that handles outbound HTTP communication with the simulator.

#### MICROSERVICE: rest-api
- TYPE: backend
- DESCRIPTION: Exposes all the HTTP endpoints consumed by the frontend. Provides access to in-memory cache to always provide the latest update state of actuators and sensors. Accepts manual toggle command of the actuators and provides a POST call to the simulator's API too update the state. Handles full CRUD operations on the automation rules persited in PostgresSQL and is supported by a SSE stream that provides push notifications to the frontend whenever new information arrives.
- PORTS: 8081
- TECHNOLOGICAL SPECIFICATION:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
It uses spring-boot-starter-web to use the annotations:
@RestController to initialize a rest controller component
@RequestMapping("/api") to define the parent api endpoint
@CrossOrigin(origin = "*") to expose the REST interface to the frontend
Still spring-boot-starter-web is used to implement a SseEmitter for the SSE stream.
The build process is managed by Apache Maven.
- SERVICE ARCHITECTURE: 
The service is realized with an ApiController class containing all endpoint mappings and an SseEmitterService class that manages the list of active SSE connections and broadcasts events to all of them.
- ENDPOINTS:

  | HTTP METHOD | URL | Description | User Stories |
	| ----------- | --- | ----------- | ------------ |
  | GET | /api/sensors | Returns all latest sensor values from in-memory cache | 1,4 |
  | GET | /api/actuators | Returns all actuator states with last update timestamp | 2 |
  | POST | /api/actuators/{name}/toggle | Manually sets actuator state, forwards command to simulator | 5 |
  | GET | /api/actuators/{name}/rules | Returns all rules for a specific actuator | 7 |
  | POST | /api/actuators/{name}/rules | Creates a new automation rule and persists it to PostgreSQL | 6 |
  | DELETE | /api/rules/{id} | Deletes a rule by id | 7 |
  | PUT | /api/rules/{id}/toggle | Toggles the enabled flag of a rule | 8 |
  | GET | /api/events | SSE stream — pushes a notification to the frontend on every new sensor event | 1,10 |

## CONTAINER_NAME: database

### DESCRIPTION:
Provides persistent relational storage for automation rules used by the processing-service.

### USER STORIES:
6) As a Habitat Operator, I want to create automation rules, so that the system reacts automatically to sensor changes

7) As a Habitat Operator, I want to update and manage rules in real time, so that I can regulate habitat conditions to my liking

8) As a Habitat Operator, I want to toggle rules ON/OFF without deleting them, so that I can temporarily disable automations

9) As a Habitat Operator, I want rules to persist across system restarts, so that automations survive system failures

### PORTS:
5432:5432

### DESCRIPTION:
The database container runs PostgreSQL and serves as the only persistence layer of the platform. It stores the automation rules table, which is read and written exclusively by the processing-service via Spring Data JPA. The schema is managed automatically by Hibernate on startup using ddl-auto=update. On first startup with an empty database, the DataInitializer component seeds 4 default rules to ensure the system is immediately operational.

### PERSISTENCE EVALUATION:
The database container requires persistent storage. A named Docker volume (postgres_data) is mounted at /var/lib/postgresql/data to ensure rules survive container restarts and system failures.

### EXTERNAL SERVICES CONNECTIONS:
The database container does not connect to external services. It only accepts inbound connections from the processing-service on port 5432.

### MICROSERVICES:

#### MICROSERVICE: postgres
- TYPE: database
- DESCRIPTION: Stores automation rules used by the rule engine. Seeded with 4 default rules on first startup.
- PORTS: 5432
- TECHNOLOGICAL SPECIFICATION:
PostgreSQL 15. Schema managed by Hibernate (ddl-auto=update). Accessed by the processing-service via Spring Data JPA using the HikariCP connection pool.

- DB STRUCTURE:
Rule : | id | sensor_id | metric | operator | threshold | actuator_name | actuator_state | enabled |

## CONTAINER_NAME: activemq

### DESCRIPTION:
Message broker that routes normalized sensor events from the ingestion-service to the processing-service.

### USER STORIES:
1) As a Habitat Operator, I want to see the latest value of each sensor in real time, so that I can monitor habitat conditions

### PORTS:
61616:61616, 8161:8161

### DESCRIPTION:
ActiveMQ acts as the central message broker of the platform. The ingestion-service publishes one message per sensor per polling cycle to the sensor.events queue. The processing-service consumes messages from the same queue via a JMS listener. This decouples the two services so they can operate independently.

### PERSISTENCE EVALUATION:
ActiveMQ persists messages internally using its default KahaDB store. No additional persistence configuration is required.

### EXTERNAL SERVICES CONNECTIONS:
The activemq container does not connect to external services.

### MICROSERVICES:

#### MICROSERVICE: activemq
- TYPE: middleware
- DESCRIPTION: Routes sensor event messages from the ingestion-service to the processing-service via the sensor.events queue.
- PORTS: 61613(stomp), 61616 (JMS), 8161 (Web Console)
- QUEUE: sensor.events

## CONTAINER_NAME: sensor-reading

### DESCRIPTION:
Implements the reading of all the sensors, translates the original message format of the simulator to a normalized format, connects to the ActiveMQ broker and acts as a JMS message producer for the sensors.

### USER STORIES

1) As the habitat operator, I want to see the latest value of each sensor in real time so I can monitor habitat conditions.
4) As the habitat operator, I want to see a timestamp of the last actuator state update so I know how fresh the data is
10) As the habitat operator, I want to see a visual alert when a sensor exceeds a threshold.

### PORTS
None (internal only)

### DESCRIPTION
The sensor-reading container provides the services to read from the simulated sensors by using GET requests, connect to the ActiveMQ broker through the "stomp.py" interface, translate the different JSON messages from the sensors to a normalized format and send the normalized messages to the ActiveMQ queue.

### PERSISTANCE EVALUATION
The sensor-reading container does not require data persistance to read, translate and forward messages.

### EXTERNAL SERVICES CONNECTION
The sensor-reading service connects to an ActiveMQ broker via port 61613, acting as a message producer for the broker's queue

### MICROSERVICES:

#### MICROSERVICE: message_converter
- TYPE: backend
- DESCRIPTION: Converts the four message formats from the sensors ("rest.scalar.v1", "rest.chemistry.v1", "rest.particulate.v1", "rest.level.v1") to a normalized format, that is the one of "rest.chemistry.v1" since it was the most generic one.
- PORTS: None (internal only)
- TECHNOLOGICAL SPECIFICATION:
Developed in the Python programming language using its standard libraries, in particular the "json" library, which provided the "dumps" method that serializes the argument to a JSON formatted string.
-SERVICE ARCHITECTURE:
The microservice is structured as a Python function that takes as inputs the dictionary representing the JSON message and the string representing the message type ("scalar", "chemistry", "particulate" or "level"), builds the normalized message by reassigning the fields and outputs it as a JSON formatted string.

#### MICROSERVICE: producer_to_broker
- TYPE: backend
- DESCRIPTION: Provides the interface to initiate a connection to, send messages to and disconnect from an ActiveMQ broker
- PORTS: None (internal only)
- TECHNOLOGICAL SPECIFICATION:
Developed in the Python programming language using the "stomp.py" additional library, which provided all the methods to initialize a connection, send messages to an ActiveMQ queue and disconnect from it.
- SERVICE ARCHITECTURE:
The microservice is structured as three Python function: "connect", which takes the broker host address and the port in input, initializes the connection through the "stomp.Connection12" function and start the connection; "send", which takes as inputs the connection variable, the destination queue, the body of the messages and other optional headers and sends the actual message to the indicated queue using the "application/json" MIME type; "disconnect", which simply disconnect the program from the broker.

#### MICROSERVICE: producer_main
- TYPE: backend
- DESCRIPTION: Coordinates the initialization of the connection with the ActiveMQ broker, the reads of all the sensors and the forwarding of the normalized messages tp the broker (carried out every 3 seconds).
- PORTS: None (internal only)
- TECHNOLOGICAL SPECIFICATION:
Developed in the Python programming language using the "requests" additional library, which provided all the methods to make HTML requests towards a given host.
- SERVICE ARCHITECTURE:
The microservice initializes the global variables for the host and the port by fetching the environmental variables declared in the docker-compose file, initializes and start the connection with the ActiveMQ broker, then starts a loop in which makes a HTML GET request to read each sensor one by one every 3 seconds from the simulated environment (using the "requests.get" function) and sends the normalized message to the destination queue. In case of error, it dismisses the connection and retries in the next loop.

## CONTAINER_NAME: frontend

### DESCRIPTION:
Implements the presentation layer of the system by showing in a single practical page the readings from the sensors and the status of the actuators. Another page is implemented to show and modify the rules for the actuators.

### USER STORIES

1) As the habitat operator, I want to see the latest value of each sensor in real time so I can monitor habitat conditions.
2) As the habitat operator, I want to see the current state of all actuators so I know what is active.
4) As the habitat operator, I want to see a timestamp of the last actuator state update so I know how fresh the data is
5) As the habitat operator, I want to toggle and untoggle actuators so that i can regulate habitat conditions also manually.
6) As the habitat operator, I want to implement automatation rules so that the system can react automatically to sensor changes.
7) As the habitat operator, I want to update rules in real time so that i can regulate the habit conditions to my liking.
8) As the habitat operator, I want to toggle and untoggle rules in real time so that i can regulate the habitat conditions to my liking.
10) As the habitat operator, I want to see a visual alert when a sensor exceeds a threshold.

### PORTS
80:80

### PERSISTANCE EVALUATION
The frontend container does not require data persistance to show the values of sensors, the status of actuators and to modify the rules.

### EXTERNAL SERVICES CONNECTION
The frontend service connects to the Google API to fetch the fonts and styles to use in the CSS of the web page. Internally, it also connects to the processing service on "http://localhost:8081/api".

### MICROSERVICES:

#### MICROSERVICE: frontend-page
- TYPE: frontend
- DESCRIPTION: This microservice serves the main user interface for the habitat operator.
- PORTS: 80:80
- TECHNOLOGICAL SPECIFICATION: 
Developed in the HTML, CSS and JavaScript languages. In particular every variable field is updated in real time by using the JavaScript function "fetch", that communicates with the processing service on the provided API endpoints.
- PAGES:
	| Name | Description | Related Microservice | User Stories |
	| ---- | ----------- | -------------------- | ------------ |
	| Home | Displays the sensors' values and actuators' status | message_converter, jms-consumer, rest-api, message_converter, producer_to_broker, producer_main | 1, 2, 4, 5, 10 |
  | Rules | Displays the rules of the selected actuator and an interface to add them | rule-engine, postgres | 6, 7, 8 |
