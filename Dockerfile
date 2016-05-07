FROM java:8

COPY target/healthcheck-watcher-0.1-SNAPSHOT.jar .
COPY healthcheck-watcher.yml .

EXPOSE 8080
EXPOSE 8081
CMD ["java","-jar","healthcheck-watcher-0.1-SNAPSHOT.jar","server","healthcheck-watcher.yml"]

