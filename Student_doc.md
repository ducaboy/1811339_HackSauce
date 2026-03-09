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

{
  "type": "object",
  "required": ["sensor_id", "captured_at", "measurements", "status"],
  "properties": {
    "sensor_id": { "type": "string" },
    "captured_at": { "type": "string", "format": "date-time" },
    "measurements": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["metric", "value", "unit"],
        "properties": {
          "metric": { "type": "string" },
          "value": { "type": "number" },
          "unit": { "type": "string" }
        }
      }
    },
    "status": { "type": "string", "enum": ["ok", "warning"] }
  }
}

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
The processing-service is the core backend of the platform. It acts as JMS Consumer to fetch normalized data from an Active MQ broker, updates in-memory cache of the sensors' state with the latest readings. On new incoming messages it loads the rules from the PostgreSQL database and evaluates each one. When a condition is a POST request is sent to the original server to change the state of the actuatos. It also exposes a REST API, which working together with SSE, is able to provide data about actuators and sensors in real-time to the frontend. It also allows to perform CRUD operations on the rules stored, persistantly, in the database.

### PERSISTANCE EVALUATION
The processing-service require persistent storage of the automation rules. Every time new measurements arrive the rules are loaded from the database and evaluated. A system of in-memory caching is also implemented storing the last value of the sensors' state, but not tracking their history.

### EXTERNAL SERVICES CONNECTION
The processing-service connects to an ActiveMQ broker via port 61616 through a message consumer, to PostgreSQL via port 5432 and the Mars simulator via port 8080.

### MICROSERVICES:

#### MICROSERVICE: jms-consumer
-TYPE: backend
-DESCRIPTION: Listens to the sensor.events queue on ActiveMQ. On every incoming message it deserializes the JSON payload into a SensorEvent object, updates the in-memory sensor cache with the latest reading for that sensor, triggers the rule engine to evaluate all enabled rules against the new event, and pushes an SSE notification to all connected frontend clients.
-PORTS: None (internal only)
-TECHNOLOGICAL SPECIFICATION:
Developed in Java using Spring Boot. Uses the @JmsListener annotation to subscribe to the sensor.events queue. Uses Jackson ObjectMapper for JSON deserialization. Handles both BytesMessage and TextMessage JMS message types for broker compatibility.
-SERVICE ARCHITECTURE:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
Key components of the stack:
Spring Boot:
Several Spring Boot starter dependencies are included:
spring-boot-starter-web: This is used for building web applications, including RESTful APIs.
spring-boot-starter-test: This is included for unit and integration testing purposes.
spring-boot-starter-activemq: This is to establish an ActiveMq connection.
The build process is managed by Apache Maven.
The service is realized with a single SensorConsumer class annotated with @Component. It holds references to the SensorStateCache, RuleEngine, and SseEmitterService and coordinates the full processing pipeline on each incoming message.

#### MICROSERVICE: rule-engine
-TYPE: backend
-DESCRIPTION: Evaluates rules against the incoming sensor event data and triggers the actuators' state change if the conditions are met. On each evaluation it loads the rules from the PostgreSQL database by using an interface, which maps the rules table to a Java object. If a condition is met a REST Post call on the simulator is made to update the state of the actuator.
PORTS: None (internal only)
-TECHNOLOGICAL SPECIFICATION:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
Uses Spring Data JPA to load rules from PostgreSQL. Uses RestTemplate to send actuator commands to the simulator REST API. Actuator states are cached in memory using a ConcurrentHashMap. The ActuatorClient initializes all 4 actuator states to OFF on startup via @PostConstruct.
The build process is managed by Apache Maven.
-SERVICE ARCHITECTURE:
The service is realized with a RuleEngine class that contains the condition evaluation logic and an ActuatorClient class that handles outbound HTTP communication with the simulator.

#### MICROSERVICE: rest-api
-TYPE: backend
-DESCRIPTION: Exposes all the HTTP endpoints consumed by the frontend. Provides access to in-memory cache to always provide the latest update state of actuators and sensors. Accepts manual toggle command of the actuators and provides a POST call to the simulator's API too update the state. Handles full CRUD operations on the automation rules persited in PostgresSQL and is supported by a SSE stream that provides push notifications to the frontend whenever new information arrives.
-PORTS: 8081
-TECHNOLOGICAL SPECIFICATION:
The microservice utilizes the Java programming language, specifically targeting Java 25. The service is built using the Spring Boot framework, version 4.0.3.
It uses spring-boot-starter-web to use the annotations:
@RestController to initialize a rest controller component
@RequestMapping("/api") to define the parent api endpoint
@CrossOrigin(origin = "*") to expose the REST interface to the frontend
Still spring-boot-starter-web is used to implement a SseEmitter for the SSE stream.
The build process is managed by Apache Maven.
-SERVICE ARCHITECTURE: 
The service is realized with an ApiController class containing all endpoint mappings and an SseEmitterService class that manages the list of active SSE connections and broadcasts events to all of them.
-ENDPOINTS:

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