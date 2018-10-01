#!/bin/bash

PREFIX="MyGeneratedClass"
FILES=$1
shift

echo "Generating $FILES files"
for i in `seq 1 $FILES`; do
  FILENAME="$PREFIX$i"
  $JAVA_HOME/bin/java -jar jbgenerator.jar -filename "$FILENAME" "$@" >/dev/null
  mx native-image "$FILENAME"
done

