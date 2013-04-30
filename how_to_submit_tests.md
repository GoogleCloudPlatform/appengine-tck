How to Submit Tests
===================

Overview
--------
This document covers the steps to submit a test or make any other contribution to the GAE TCK.

Setting Up Your Local Repository
--------------------------------
First, create a fork of the GAE TCK to your github account.
    [GAE TCK fork](https://github.com/GoogleCloudPlatform/appengine-tck/fork)

Clone your fork locally
    git clone https://github.com/(username)/appengine-tck.git
    cd appengine-tck

Add the official GAE TCK as "upstream" to your clone.
    git remote add upstream https://github.com/GoogleCloudPlatform/appengine-tck.git

The previous steps are only necessary the first time you set this up.  The following steps
you will do periodically while writing and submitting tests.

Sync with the official GAE TCK via fetch.
    git fetch upstream

Rebase with the offical GAE TCK in order to keep the commit history consistent.
    git pull --rebase upstream master

Check the log to confirm your commit history is up-to-date with the official repository.
    git log

Writing Your Test
-----------------
Before writing your test it is good practice to rebase with the official master branch.
    git pull --rebase upstream master
    git log

Create a local branch and switch to it (this command switches to it automatically).
    git checkout -b the-best-tck-test

Write your test/contribution, then add and commit them locally.  For reference if you
are new to [git](http://git-scm.com/documentation).  Some commands that you will run.
    git status
    git add .
    git commit

Making the Submission/Pull-Request
---------------------------------
Sync and rebase with the official master again.
    git fetch upstream
    git pull --rebase upstream master
    git log

Compile, run, and fix tests.
    cd appengine-tck
    mvn clean install
    cd tests/appengine-tck-[modified test package]
    mvn clean install -Psdk,multisuite -Dappengine.sdk.root=<PATH_TO_SDK>

Push your branch to your fork(assuming we are still in "the-best-tck-test" branch).
    git push origin the-best-tck-test

Make a Pull-Request

    https://github.com/(username)/appengine-tck
    Click "Pull Request" button.
    Select your branch "the-best-tck-test" to be pulled into the base repo GoogleCloudPlatform:master

One of the Committers will pull your branch in to master.

Thank you for your contribution!


For Committers Only
-------------------
Three repositories are involved, 1)fork of the Pull-Request, 2)fork of the committer,
3)official master.

For example, say we want to merge this pull-request from user chucknorris: chucknorris/the-best-tck-test

One-time setup:
    git clone https://github.com/(committer-username)/appengine-tck.git
    cd appengine-tck
    git remote add upstream https://github.com/GoogleCloudPlatform/appengine-tck.git

For each user making a Pull-Request:
    git remote add chuck https://github.com/chucknorris/appengine-tck.git

Each commit:
    git fetch chuck
    git checkout -b tmp_x
    git merge chuck/the-best-tck-test
    git pull --rebase upstream master
    git checkout master
    git merge tmp_x
    mvn clean install
    cd tests
    mvn clean install -P[sdk|appspot|...],multisuite etc. (perhaps even against your favorite env)
    git push upstream master
