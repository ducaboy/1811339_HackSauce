# producer_to_broker.py
import stomp
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("activemq-producer")

def connect(host: str, port: int, wait=True):
    logger.info("Connecting to broker %s:%s", host, port)
    conn = stomp.Connection12([(host, port)])
    #conn.set_listener('', stomp.PrintingListener())
    conn.connect(wait = wait)
    logger.info("Connected")
    return conn

def send(conn, dest: str, body: str, persistent: bool = True, headers: dict | None = None):
    hdrs = {} if headers is None else dict(headers)
    if persistent:
        hdrs['persistent'] = 'true'
    # You can add custom headers here, e.g., message-id, type, correlation-id
    conn.send(destination = dest, body = body, content_type = "application/json", headers=hdrs)
    #logger.info("Sent message to %s: %s", dest, body)

def disconnect(conn):
    conn.disconnect()
    logger.info("Disconnected")
