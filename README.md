# healthcheck-watcher

Aggregate [dropwizard](https://dropwizard.github.io/dropwizard/) [health checks](https://dropwizard.github.io/dropwizard/manual/core.html#health-checks) from your system and expose one single endpoint for their combined health.

## What? 
                                                                                                           
An attempt to build a simple aggregation and visualisation of the combined health of a system of potentially clustered 
services exposing dropwizard health checks kind of health checks. 

Let your service discovery system add and remove services to the health checks watcher as services become available and 
die off, and healthchecks-watcher will give an aggregated view of your whole system and single endpoint to monitor 
for partial failures, with directions about where to go for deeper investigations.
 
## Why?

Dropwizard health checks are awesome. A nice and simple API and concept, but when you put the ability to define health
checks in hands of the developers responsible both for a service being up and running, performant enough and making the 
business the money it's supposed to (or any other health check that is relevant to the particular service) you both get 
better scalability and cohesion team wise. 
  
Somebody stills needs to keep track of all the services and their individual health checks though, and that is where
this service comes into play. Deploy it once and hook it into your service discovery mechanism and you now have a single
point of entry for all that distributed and somewhat scattered knowledge.
 
Not even sure if this makes sense yet, we'll see when it reaches production : ). Welcome to contribute with feature 
requests, input on existing issues and pull requests for features or bug fixes.
 
## How?  

...



