Google App Engine TCK
=====================

Project introduction
--------------------

This project provides Technology Compatibility Kit (TCK) for Google App Engine (GAE) API.

The goals of the project are
* comprehensive GAE API test coverage
* GAE API usage examples
* common GAE API test environment

Project can either be used by external vendors (not Google) implementing the GAE API - such as CapeDwarf and AppScale,
or developers writing frameworks on top of GAE API, or simply using GAE API.

e.g. the easiest way to file a bug against GAE API is to provide a valid failing test

License
-------

The content of this repository is released under the Apache License 2.0
as provided in the LICENSE file that accompanied this code.

By submitting a "pull request" or otherwise contributing to this repository, you
agree to license your contribution under the license mentioned above.

License terms for 3rd Party Works
---------------------------------

This software uses a number of other works, the license terms of which are
documented in the NOTICE file that accompanied this code.

Directory structure
-------------------

* `common`      - common test code; base tests and multisuite support
* `core`        - core test code; black list, endpoints, SQL, modules, ...
* `env`         - custom environment hooks; GAE SDK, Appspot, CapeDwarf, AppScale, ...
* `ext`         - external (useful) tests; e.g. DataNucleus, MapReduce, Objectify, ...
* `site`        - reporting site, exposing data from TCK TeamCity CI runs
* `tests`       - the main TCK API tests
* `utils`       - helper utils; code coverage plugin, bytecode transforemers

Requirements
------------

Java JDK7+ and Maven 3.x+.
And of course the environment you want to test.  It is assumed you have working knowledge
of Maven and Git.

Quick Start
-----------
To simply run the tests against the sdk you can follow the instructions below.  If you
are submitting a test or making any other contribution see [How to Submit Tests](how_to_submit_tests.md)

    git clone https://github.com/GoogleCloudPlatform/appengine-tck.git
    cd appengine-tck
    mvn clean install
    cd tests
    mvn clean install -Psdk,multisuite -Dappengine.sdk.root=<PATH_TO_SDK>

A summary of each API (SUCCESS/FAILURE) will be listed at the end, and the raw results will
be located in tests/appengine-tck-[test-package]/target/surefire-reports.

To generate an html report:

    mvn surefire-report:report

There will be a report for each api located in tests/appengine-tck-[test-package]/target/site/surefire-report.html.The details of the failures will are at the bottom.

The rest of this document goes into the details of running and writing tests.

Running the build
-----------------

The whole project is fully Mavenized, so for the simple build the process is obvious:

    cd appengine-tck
    mvn clean install

This builds the default profiles, which includes compiling the tests, but not running them.
And we also output the API code coverage metrics; see console output.  Before running any tests
you need to do this step in order to build the framework.  Then you can go into specific test
directories and run only those tests.  If you don't do this step and go straight to the test directory
you will get compilation errors.

Building the tests
------------------

All tests are build using [Arquillian](http://www.arquillian.org) and [ShrinkWrap](http://www.jboss.org/shrinkwrap).
Each different environment has a custom Arquillian container implementation.

GAE container implementations can be found here: https://github.com/alesj/arquillian-container-gae

CapeDwarf uses plain JBossAS Arquillian container implementations - remote and managed.
Where we recommend remote for manual (simple) testing, and managed for automated (e.g. TeamCity, Hudson, ...) testing.

GAE API code coverage
---------------------

For each set of tests we want to get code coverage, needs a coverage.txt file,
where we list all the classes / interfaces whose usage we track.  Deprecated classes,
interfaces, and methods will not be reported.

e.g. TaskQueue code covereage

    file: appengine-tck/tests/appengine-tck-memcache/coverage.txt

    com.google.appengine.api.taskqueue.Queue

This will print out all Queue interface usage in our TaskQueue tests.

You can either see the results in console while the build is running,
or at the end open index.html file in TCK root.  A csv file is also generated
which can be imported into a spreadsheet.  Each test directory has its own index.html
and csv reports.

You can override which file name is used to lookup to list classes / interfaces.
This is done either by changing the coverage plugin's configuration in pom.xml or
using -Dcoverage.file system property.  A copy of the coverage file is located in the respective
test directories.

    cd appengine-tck
    mvn clean install -Dcoverage.file=coverage.txt.all

Or just:

    cd appengine-tck
    mvn clean install

The report is located here: appengine-tck/index.html

We can also explicitly exclude some of the API methods with exclusions.txt file (deprecated methods are already excluded by default).

e.g. excluding com.google.appengine.api.urlfetch.URLFetchServiceFactory's constructor

    com.google.appengine.api.urlfetch.URLFetchServiceFactory@<init>@()V

The default exclusion information must follow this pattern:

    <fqn class name>@<method name>@<descriptor>

Running the tests
-----------------

Running the tests is same as building the project,
but you also need to specify the environment you want to test.

First, as always, build at the root directory:

    cd appengine-tck
    mvn clean install

Then either go to the tests directory to run all the tests or
the directory of the specific api, e.g. tests/appengine-tck-memcache

   cd appengine-tck/tests
or
   cd appengine-tck/tests/appengine-tck-memcache

This are the current enviroments:

1) GAE SDK

    mvn clean install -Psdk -Dappengine.sdk.root=<PATH_TO_SDK>

2) GAE Appspot

    mvn clean install -Pappspot -Dappengine.sdk.root=<PATH_TO_SDK> -Dappengine.userId=<USER_ID> -Dappengine.password=<USER_PASSWORD> -Dappengine.appId=<APPSPOT_APP_ID>

3) JBoss CapeDwarf (remote)

    mvn clean install -Pcapedwarf

Where you need a running instance of CapeDwarf environment to be present.

4) JBoss CapeDwarf (managed)

    mvn clean install -Dcapedwarf.home

Where you need CapeDwarf distribution to be present under "capedwarf.home" directory.

Deploying all tests at once
---------------------------

We learned how to run the tests.
But that will deploy one test class per deployment,
which can be time consuming for environments like Appspot.

In order to handle all tests within one deployment, we added `multisuite` profile.

    mvn clean install -Psdk,multisuite -Dappengine.sdk.root=<PATH_TO_SDK>

This will deploy all tests (per API) in a single .war file, and then test them as usual.

Note: as we create an uber .war from all tests, not all tests can be included.
Tests that are not capable of running inside uber .war, *must* be marked with @IgnoreMultisuite.

Each set of tests that we want to run -Pmultisuite on them, requires a multisuite.marker file in tests' root directory.
In this marker we can override test class regexp patter or scanning strategy; see code for more details.

In case we have some failures in some API tests, but we still want to run the whole TCK, you can ignore failures with this Maven flag

    -Dmaven.test.failure.ignore=true

Running a single test
---------------------

Since it's all Mavenized, this is trivial, as Surefire plugin already supports this.

    cd appengine-tck/tests/appengine-tck-[package]
    mvn clean install -Psdk -Dappengine.sdk.root=<PATH_TO_SDK> -Dtest=<TEST_SIMPLE_NAME>

To run a specific test method.

    mvn clean install -Psdk -Dappengine.sdk.root=<PATH_TO_SDK> -Dtest=<TEST_SIMPLE_NAME>#testTheMethod

Adapting the environment wrt tests
----------------------------------

During the tests we can fire TestLifecycle events.
Each enviroment can add (via ServiceLoader pattern) multiple TestLifecycle implementations; e.g. one per particular event type.

e.g.

    public class CapeDwarfTestContextEnhancer implements TestLifecycle {
        public void before(TestLifecycleEvent event) {
            if (event instanceof TestContextLifecycleEvent) {
                TestContextLifecycleEvent tcle = (TestContextLifecycleEvent) event;
                enhance(tcle.getTestContext());
            }
        }

API tests
----------

Current API tests:

* Blobstore
* Channel
* Datastore
* Images
* Logging
* Mail
* Memcache
* Search
* TaskQueue
* UrlFetch
* Users

Core tests
----------

This module is testing GAE built-in core functionality beyond APIs.

Current core tests:

* Endpoints support
* Miscellaneous; e.g. black list usage, etc
* Modules
* SQL

As we don't want to overload the testing, each custom core set of tests should be under unique profile.

e.g. in the case of Endpoints support we use -Pendpoints

    mvn clean install -Pcapedwarf,endpoints

This will run all Endpoints tests against CapeDwarf environment.

External tests
--------------

Here we try to gather any useful tests that will help make GAE API implementation environments better.

Current external tests:

* GAE DataNucleus Plugin
* GAE MapReduce Library
* Google Cloud Storage Client
* JUnit Example

Same as core tests, each custom external set of tests is under unique profile.

e.g. in the case of DataNucleus GAE plugin we use -Pdatanucleus

    mvn clean install -Pcapedwarf,datanucleus

This will run all DataNucleus tests against CapeDwarf environment.

e.g. running MapReduce tests against SDK

    mvn clean install -Psdk,mapreduce -Dappengine.sdk.root=<PATH_TO_SDK>

Note: adapting the tests to run against real environment can sometime be a huge (bytecode) hack. :-)

More info can be found here: [How to run external tests](ext/how_to_external_tests.md)

Running existing JUnit tests
----------------------------

In case you already have existing JUnit tests, running against local test environment; e.g. similar to DataNucleus plugin,
you can - with some bytecode magic - easily re-use those tests to run against any environment, in-container this time.

To see how that's done, see JUnit example or GAE DataNucleus Plugin external testing.

Writing the test
----------------
See appengine-tck/tests/appengine-tck-example for a bare bones illustration of how to write a
new test package.

As we already mentioned, all tests are Arquillian and ShrinkWrap based,
with JUnit being the actual test framework used, and Maven Surefire plugin to run it all.

But there are a few guidelines we need to follow, specially with regard to "multisuite".

Few basic Arquillian rules:
* each test class needs to have @RunWith(Arquillian.class) in its class hierarchy
* there must be exactly one (in whole class hierarchy) static method marked with @Deployment, returning WebArchive instance

Few basic ShrinkWrap rules:
 * do not forget to add base test classes to deployment, only actual test class is added by default
 * make sure .war's META-INF is in WEB-INF/classes/META-INF, as per web spec

Few basic "multisuite" rules:
* we merge all WebArchive instances from all tests into one single uber WebArchive instance
* luckily there is a notification filter, which spits out warning by default if resources overlap
* notifications can be changed to failure if needed, for more strict resource usage in tests
* test classes can be excluded from "multisuite" with @IgnoreMultisuite

Why all this fuss about "multisuite"?
Imagine two different web.xml files, where one test needs servlet A, and the other one needs servlet B.
In multisuite, there is only one web.xml - first come, first serve - so one test could miss its servlet.
Which means we need to carefully craft test recources.
This mostly results in more middle / abstract classes, holding common resources.

To make things a bit easier, we already introduced a few abstract / helper classes and methods.
* each test class should extend TestBase class
* to get WebArchive instance use TestBase::getTckDeployment with proper TestContext instance
* if any custom behavior needs to be added per test environment, add this via TestLifecycles::before or TestLifecycles::after
* LibUtils class can be used to grab whole libraries / jars from Maven and attach them to .war as library (WEB-INF/lib)
* one can add its own multisuite ScanStrategy and NotificationFilter; see ScanMultiProvider class for more details

Checkstyle
----------
The maven-checkstyle-plugin is enabled for this project, and will report a
build failure if there is a violation.

Running it is the same as code coverage:

    cd appengine-tck
    mvn clean install

To generate an html report:

    mvn site

The results will be located in tests/appengine-tck-[test-package]/target/site/checkstyle.html.


Reporting Summary
-----------------
To summarize, there are 3 kind of reports that are generated:
* Code Coverage,
* Checkstyle,
* Test Results.

### Code Coverage and Checkstyle Reports

    cd appengine-tck
    mvn clean install

Code coverage will be located in index.html file.

To generate Checkstyle reports:

    mvn site

See tests/appengine-tck-[test-package]/target/site/checkstyle.html

### Test Results

    cd appengine-tck/tests
    mvn clean install -Psdk,...   (run tests)
    mvn surefire-report:report

See tests/appengine-tck-[test-package]/target/site/surefire-report.html

Site module
-----------

Site module is made up from two parts:
* reporting web app
* a TeamCity plugin that pushes the build info to the reporting web app when done.

The reporting web app shows nice pie charts for all environments, with drill-down info details on failed tests.
