```bash
$ curl 'http://localhost:8080/events' -i -X POST \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H 'Accept: application/json;charset=UTF-8' \
    -d '{
  "str" : "xxx",
  "num" : 1,
  "@type" : "TYPE_3"
}'
```