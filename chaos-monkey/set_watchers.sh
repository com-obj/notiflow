#!/bin/bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/watchers -H 'Content-Type: application/json' -d "$1"