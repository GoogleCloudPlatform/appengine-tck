Google App Engine Reporting Site
================================

The reporting site sub-module has two parts: TeamCity Server Plugin and a web application with nice reports.

The TeamCity Server Plugin pushes TeamCity build results to this web app,
which then uses nice graphs to show the results.

The web app can be accessed under [www.appengine-tck.org](http://www.appengine-tck.org/).

How do I update the web app?
----------------------------

    mvn clean install appengine:update -Psite
