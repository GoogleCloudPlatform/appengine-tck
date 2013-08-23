#!/bin/bash

# Utility to upload a simple app, or rollback a stuck update.

if [ $# -lt 2 ]; then
  echo "usage: simple-app.sh APP_ID update|rollback [VERSION] [appcfg OPTIONS]"
  echo "       Uses --oauth2."
  echo
  echo "Ex: simple-app.sh my123app update"
  echo "    simple-app.sh my123app rollback"
  exit
fi

APP_ID=$1
shift 1

APPCFG_ACTION=$1
shift 1

APP_VERSION=$1
if [ "$APP_VERSION" == "" ]; then
  APP_VERSION=1
else
  shift 1
fi

function runSimpleAppcfgAction() {

  local WAR_DIR=/tmp/simpleappcmd.$$
  mkdir -p $WAR_DIR/WEB-INF

  cat <<EOF > ${WAR_DIR}/WEB-INF/appengine-web.xml
<?xml version="1.0" encoding="utf-8"?>
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
  <application>${APP_ID}</application>
  <version>${APP_VERSION}</version>
  <threadsafe>true</threadsafe>
</appengine-web-app>
EOF

  cat <<EOF > ${WAR_DIR}/WEB-INF/web.xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee" version="2.5">
</web-app>
EOF

  cat <<EOF > ${WAR_DIR}/hello.jsp
simple-app.sh <br/>
jsp compiled: $(date) <br/>
current time: <%= System.currentTimeMillis() %>
EOF

  set -x
  appcfg.sh --oauth2 --application=${APP_ID} $@ ${APPCFG_ACTION} ${WAR_DIR}
  set +x

  rm -rf ${WAR_DIR}
}

runSimpleAppcfgAction $@
