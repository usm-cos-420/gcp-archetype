appengine-standard-archetype
============================

This is a generated App Engine Standard Java application from the appengine-standard-archetype archetype.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/


* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) --> [Java 11] (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/download.cgi) (at least 3.5)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

This document augments the tutorial instructions : https://docs.google.com/document/d/1cqam-V7TtvvltCp7t-ZryNjhq2hNeu_HEM-AzgWw9j0.  We extend the tutorial to support jsp forms and Postgres insert operation. See commits for details on the changes.  

## Setup

    gcloud init
    gcloud auth application-default login

## Maven
### Running locally

    mvn clean install appengine:run -DskipTests

### Deploying

    mvn clean install appengine:deploy -DskipTests


## Updating to latest Artifacts

An easy way to keep your projects up to date is to use the maven [Versions plugin][versions-plugin].

    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    mvn versions:use-latest-versions

