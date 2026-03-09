# sensor_reader.py
import requests
#import copy
import json

def sensor_msg_converter(msg: dict, type: str):
    #"type" is a string that references the sensor types contained in SCHEMA_CONTRACT.md
    #"msg" is the request message converted to a dictionary by json()
    if type == "scalar":
        normalized_measurement_1 = {"metric": msg["metric"], "value": msg["value"], "unit": msg["unit"]}
        normalized_message = {"sensor_id": msg["sensor_id"], "captured_at": msg["captured_at"], "measurements": [normalized_measurement_1], "status": msg["status"]}
        return json.dumps(normalized_message)
    
    elif type == "chemistry":
        #In this case, since we chose the "rest.chemistry.v1" type as our standard, we pass the message as it is
        return json.dumps(msg)
    
    elif type == "particulate":
        normalized_measurement_1 = {"metric": 'pm1', "value": msg["pm1_ug_m3"], "unit": 'ug_m3'}
        normalized_measurement_2 = {"metric": 'pm25', "value": msg["pm25_ug_m3"], "unit": 'ug_m3'}
        normalized_measurement_3 = {"metric": 'pm10', "value": msg["pm10_ug_m3"], "unit": 'ug_m3'}
        normalized_message = {"sensor_id": msg["sensor_id"], "captured_at": msg["captured_at"], "measurements": [normalized_measurement_1, normalized_measurement_2, normalized_measurement_3], "status": msg["status"]}
        return json.dumps(normalized_message)
    
    elif type == "level":
        normalized_measurement_1 = {"metric": 'level_pct', "value": msg["level_pct"], "unit": 'pct'}    #"pct" is the percentage of remaining water
        normalized_measurement_2 = {"metric": 'level_liters', "value": msg["level_liters"], "unit": 'liters'}
        normalized_message = {"sensor_id": msg["sensor_id"], "captured_at": msg["captured_at"], "measurements": [normalized_measurement_1, normalized_measurement_2], "status": msg["status"]}
        return json.dumps(normalized_message)
    
    else:
        print("Message type not valid")
        return ""
