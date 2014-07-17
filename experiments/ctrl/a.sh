curl -d '{"switch": "00:a4:23:05:00:00:00:01", "name":"t1-1", "priority":"1", "active":"true", "actions":""}' http://localhost:10001/wm/staticflowentrypusher/json
#curl -d '{"switch": "00:a4:23:05:00:00:00:01", "name":"t1-1", "priority":"1", "ether-type":"2048", "src-ip":"196.188.0.0/16", "dst-ip":"38.40.0.0/8", "active":"true", "actions":"set-dst-ip=200.10.10.12"}' http://localhost:10001/wm/staticflowentrypusher/json

