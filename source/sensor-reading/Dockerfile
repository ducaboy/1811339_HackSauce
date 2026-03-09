# Stage 1 — Build
FROM python:3.11-slim

# Set working directory
WORKDIR /app

RUN pip install --no-cache-dir stomp.py requests
COPY . .
# Run the app when the container launches
CMD ["python" , "producer_main.py"]