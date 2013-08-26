#!/bin/bash

# Utility to copy latest app engine java sdk and create a sym link to it.

if [ $# -lt 1 ]; then
  echo "usage: get-latest-java-sdk.sh DIRECTORY"
  echo "       cd to DIRECTORY, then creates symlink appengine-java-sdk to the version downloaded."
  exit
fi

GAE_SDK_DIR=$1
cd $GAE_SDK_DIR

# Fetch download page.
GAE_DL_PAGE=/tmp/ae-download-page.$$
curl -s 'https://developers.google.com/appengine/downloads' -o $GAE_DL_PAGE

GAE_ZIP_LINK=$(sed -n -e 's/^.*href="\(http:\/\/googleappengine\.googlecode\.com\/files\/appengine-java-sdk-.*\)".*$/\1/p' $GAE_DL_PAGE)
if [[ "$GAE_ZIP_LINK" == "" ]]; then
  echo "download page not valid."
  exit 1
fi

# Parse SDK version on dowload page.
GAE_ZIP_VERSION=$(echo $GAE_ZIP_LINK | sed -n -e 's/^http:\/\/googleappengine\.googlecode\.com\/files\/appengine-java-sdk-\(.*\)\.zip.*$/\1/p')
if [[ "$GAE_ZIP_VERSION" == "" ]]; then
  echo "version not valid:"
  exit 1
fi

# Parse SDK version in current directory.
GAE_SDK_SYMLINK=appengine-java-sdk
GAE_SDK_ZIP_FILE=${GAE_SDK_SYMLINK}-${GAE_ZIP_VERSION}.zip
GAE_CURRENT_VERSION=$(readlink ${GAE_SDK_SYMLINK} | sed -n -e 's/appengine-java-sdk-\(.*\)$/\1/p')

if [[ "$GAE_ZIP_VERSION" == $GAE_CURRENT_VERSION ]]; then
  echo "version already up-to-date: ${GAE_ZIP_VERSION}"
  exit 0
fi

# Download latest version.
curl -O $GAE_ZIP_LINK  # this should download a file that matches $GAE_SDK_ZIP_FILE
if [[ "$?" != "0" ]]; then
  echo "Unable to download $GAE_ZIP_LINK"
  exit 1
fi

# Unzip SDK.
unzip -q $GAE_SDK_ZIP_FILE
if [[ "$?" != "0" ]]; then
  echo "Unable to unzip ${GAE_SDK_ZIP_FILE}"
  exit 1
fi

# Update symlink.
rm -f ${GAE_SDK_SYMLINK}
ln -s appengine-java-sdk-${GAE_ZIP_VERSION} ${GAE_SDK_SYMLINK}

# Verify unzipped directory matches version.
head ${GAE_SDK_SYMLINK}/RELEASE_NOTES | grep ${GAE_ZIP_VERSION}
if [[ "$?" != "0" ]]; then
  echo "WARNING! RELEASE_NOTES does not contain expected version: ${GAE_ZIP_VERSION}"
fi

rm -f $GAE_DL_PAGE
