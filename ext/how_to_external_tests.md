GAE TCK External Tests
======================

Overview
--------
This document goes into further detail about the external tests module.

Types of External Tests
-----------------------
Tests that do not fit in the core App Engine API set will fall into
this category.  Currently we have 4 external test suites.

* datanucleus - This is the [DataNucleus plugin for Google App Engine](https://code.google.com/p/datanucleus-appengine/)

It contains tests that use the [Local Unit Testing Utilities for App Engine](https://developers.google.com/appengine/docs/java/tools/localunittesting)  The transformer classes remove the service stubs and add the TCK test annotations so the tests can run on appspot including other implementations.  These tests are built separately from the TCK.  The tests are found via maven with the package name, the pom.xml declares these dependencies as well as the transformer itself.

* mapreduce - These tests are for the [Java MapReduce for App Engine](https://code.google.com/p/appengine-mapreduce/wiki/GettingStartedInJava)

The tests are annotated to run with the TCK framework.  Because it is not part of the core GAE Apis it is inclued here.  The tests themselves are checked in as well.

* gcs-client - These tests are for the [Java Google Cloud Storage Client Library](https://code.google.com/p/appengine-gcs-client/).

* example - This module contains a simple junit tests and illustrates how you would write your own transformer, like the one for datanucleus.


Running DataNucleus Tests
-------------------------

    svn checkout http://datanucleus-appengine.googlecode.com/svn/branches/2_1_x
    cd 2_1_x/tests
    mvn clean install
    cd ../appengine-tck (i.e. your appengine-tck location)
    mvn clean install
    cd ext/datanucleus
    mvn clean install -Pappspot,datanucleus


Running MapReduce Tests
-----------------------

    cd appengine-tck
    mvn clean install
    cd ext/mapreduce
    mvn clean install -Psdk,mapreduce -Dappengine.sdk.root=<PATH_TO_SDK>

Running Google Cloud Storage Client Tests
-----------------------------------------

    cd appengine-tck
    mvn clean install
    cd ext/gcs-client
    mvn clean install -Pcapedwarf,gcs-client


Running the Example
-------------------

    cd appengine-tck
    mvn clean install   (this builds the TCK framework and the Transformer classes)
    cd ext/example
    mvn clean install -Pappspot,ext-junit-example

We have one test, that now goes against real in-container DatastoreService.
