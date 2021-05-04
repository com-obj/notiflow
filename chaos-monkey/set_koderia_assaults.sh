#!/bin/bash
/bin/bash ./set_assaults.sh '{
  "level": 2,
  "latencyRangeStart": 2000,
  "latencyRangeEnd": 6000,
  "latencyActive": true,
  "exceptionsActive": true,
  "killApplicationActive": false,
  "runtimeAssaultCronExpression": "OFF",
  "watchedCustomServices": []
}'