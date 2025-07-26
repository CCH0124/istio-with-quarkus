#!/bin/bash
while true; do curl -s -H "Host: istio-lab.cch.com" "http://istio-lab.cch.com:9080/hello/info"; sleep 2; done