# Docker based integration tests 
We all write integration tests which sometimes depends on external systems for end to end execution. These systems can be a external API or a database call.
Often these systems are not available in pre-prod environment which results in test failures.
With dockerized backend systems we can make sure that our integration tests are using our docker containers for testing and  are not dependent on the pre-prod environment.

## Prerequisite 
* Create a docker container for your extenral system eg. database or an API
* Push it to a repo so that it can be pulled from all your systems executing tests 

## Steps
* Create a docker manager using spotify docker client to start and stop containers from your Java code
* Start containers before your tests start for example using @BeforeClass annotation of Junit
* Run your tests by pointing your connection configuration to localhost so that it can use the container based instance
* Stop the container once all the tests are executed.
