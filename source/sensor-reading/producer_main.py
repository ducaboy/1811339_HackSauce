# producer_main.py
import os
import requests
import json
import time
from message_converter import sensor_msg_converter
from producer_to_broker import *

BROKER_HOST = os.environ.get('STOMP_HOST', '127.0.0.1')
BROKER_PORT = int(os.environ.get('STOMP_PORT', 61613))      #DA CAMBIARE DURANTE TESTING, provare 61616
SIMULATOR_URL = os.environ.get('SIMULATOR_URL', 'http://localhost:8080')
#USERNAME = "admin"
#PASSWORD = "admin"
DESTINATION = "sensor.events"      #DA CAMBIARE DURANTE TESTING
SEND_TIMER = 5.0

def get_connection():
    while True:
        try:
            return connect(BROKER_HOST, BROKER_PORT, True)
        except Exception as e:
            logger.error("Could not connect, retrying in 5s: %s", e)
            time.sleep(5)

connection = get_connection()

while True:
    if not connection.is_connected():
        logger.info("Reconnecting...")
        connection = get_connection()
        
    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/greenhouse_temperature").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/entrance_humidity").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/co2_hall").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/corridor_pressure").json()
        norm_msg = sensor_msg_converter(msg, "scalar")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/hydroponic_ph").json()
        norm_msg = sensor_msg_converter(msg, "chemistry")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/air_quality_voc").json()
        norm_msg = sensor_msg_converter(msg, "chemistry")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/air_quality_pm25").json()
        norm_msg = sensor_msg_converter(msg, "particulate")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    try:
        msg = requests.get(f"{SIMULATOR_URL}/api/sensors/water_tank_level").json()
        norm_msg = sensor_msg_converter(msg, "level")
        send(connection, DESTINATION, norm_msg, True, None)
    except Exception as e:
        logger.exception("Producer error: %s", e)
        disconnect(connection)

    time.sleep(SEND_TIMER)