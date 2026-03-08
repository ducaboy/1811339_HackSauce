# producer_main.py
import requests
import json
import time
from message_converter import sensor_msg_converter
from producer_to_broker import *

BROKER_HOST = "127.0.0.1"
BROKER_PORT = 61616      #DA CAMBIARE DURANTE TESTING, provare 61616
#USERNAME = "admin"
#PASSWORD = "admin"
DESTINATION = "/sensor/events"      #DA CAMBIARE DURANTE TESTING
SEND_TIMER = 5.0

connection = connect(BROKER_HOST, BROKER_PORT, True)

while True:
    try:
        msg = requests.get("http://localhost:8080/api/sensors/greenhouse_temperature").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/entrance_humidity").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/co2_hall").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/corridor_pressure").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/hydroponic_ph").json()
        norm_msg = sensor_msg_converter(msg, "chemistry")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/air_quality_voc").json()
        norm_msg = sensor_msg_converter(msg, "chemistry")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/air_quality_pm25").json()
        norm_msg = sensor_msg_converter(msg, "particulate")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get("http://localhost:8080/api/sensors/water_tank_level").json()
        norm_msg = sensor_msg_converter(msg, "level")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    time.sleep(SEND_TIMER)