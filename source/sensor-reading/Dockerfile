# Stage 1 — Build
FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir stomp.py requests

COPY . .

# Run the app when the container launches
ENTRYPOINT ["python"]
CMD ["producer_main.py"]