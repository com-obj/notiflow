#!/bin/bash
/bin/bash ./set_assaults.sh '{
  "level": 1,
  "latencyRangeStart": 2000,
  "latencyRangeEnd": 5000,
  "latencyActive": true,
  "exceptionsActive": true,
  "exception": {
    "type": "java.lang.IllegalArgumentException",
    "arguments": [{
      "className": "java.lang.String",
      "value": "custom illegal argument exception"}] },
  "killApplicationActive": false,
  "runtimeAssaultCronExpression": "OFF",
  "watchedCustomServices": ["com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinder.apply"]
}'