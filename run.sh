#!/bin/sh

docker build -t healthchecks-watcher .

docker run -d -P --name healthchecks-watcher healthchecks-watcher
