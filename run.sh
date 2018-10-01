#!/bin/bash

./create-jar.sh

FILES=$1
shift

echo "Generating $FILES files"
for i in `seq 1 $FILES`; do
  $JAVA_HOME/bin/java -jar jbgenerator.jar -filename "MyGeneratedClass$i" "$@"
done
