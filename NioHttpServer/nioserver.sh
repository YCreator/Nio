#!/bin/bash
nohup java -jar NioHttpServer1.jar 8844 >/dev/null 2>&1 &
nohup java -jar NioHttpServer2.jar 8845 >/dev/null 2>&1 &
nohup java -jar NioHttpServer3.jar 8846 >/dev/null 2>&1 &
nohup java -jar NioHttpServer4.jar 8847 >/dev/null 2>&1 &