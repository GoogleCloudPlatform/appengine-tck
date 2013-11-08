#!/bin/bash
# Install GAE sdk into local maven repository.
# Used internally at Google when testing versions before publicly available.

if [ "$#" -lt 1 ]; then
  echo "usage: install_sdk.sh -[lsv]"
  echo "       -l list installed jars under ~/.m2"
  echo "       -s sdk_root"
  echo "       -v version"
  exit
fi

# ARTIFACT_IDS pair with the JAR pattern

ARTIFACT_IDS=(appengine-api-1.0-sdk appengine-tools-sdk appengine-endpoints appengine-testing appengine-api-stubs appengine-api-labs)
JAR_PATTERN=(appengine-api-1.0-sdk-*.jar appengine-tools-api*.jar appengine-endpoints.jar appengine-testing.jar appengine-api-stubs.jar appengine-api-labs-*.jar)
GROUP_ID=com.google.appengine
ARTIFACT_COUNT=${#ARTIFACT_IDS[@]}

function listRepository() {
  pushd ~ > /dev/null
  for i in "${ARTIFACT_IDS[@]}" ; do
    echo $i
    find .m2/repository/com/google/appengine/${i} -name "*.jar"
    echo
  done
  popd > /dev/null
}

while getopts ls:v: option
do
    case "${option}"
    in
        l) LIST=true;;
        s) SDK_ROOT=${OPTARG};;
        v) VERSION=${OPTARG};;
    esac
done

echo "GROUP_ID=$GROUP_ID"
echo "SDK_ROOT=$SDK_ROOT"
echo "VERSION=$VERSION"
echo

if [ "$LIST" != "" ]; then
    listRepository
    exit
fi

if [ ! -d $SDK_ROOT ]; then
     echo "$SDK_ROOT does not exist."
     exit 1
fi

if [ -z $VERSION ]; then
    echo "Missing version. Example: -v 1.7.7"
    exit 1
fi

ARTIFACT_COUNT_LIMIT=$(( $ARTIFACT_COUNT - 1 ))
for i in $(seq 0 $ARTIFACT_COUNT_LIMIT); do
  artifactId=${ARTIFACT_IDS[$i]}
  jarLocation=$(find $SDK_ROOT/lib -name "${JAR_PATTERN[$i]}")
  if [ "$jarLocation" == "" ]; then
    echo "Missing jar: ${JAR_PATTERN[$i]}"
    exit 1
  fi

  echo "Installing: $artifactId $jarLocation"
  mvn install:install-file -DgroupId=${GROUP_ID} \
      -DartifactId=${artifactId} \
      -Dversion=${VERSION} \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -Dfile=${jarLocation}
done

listRepository
