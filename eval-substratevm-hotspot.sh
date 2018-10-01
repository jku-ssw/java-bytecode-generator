#!/bin/bash

PREFIX="MyGeneratedClazz"
FILES=$1
shift

echo "Generating $FILES files"
for i in `seq 1 $FILES`; do
  FILENAME="$PREFIX$i"
  $SUBSTRATEVM_JAVA_HOME/bin/java -jar jbgenerator.jar -filename "$FILENAME" "$@" >/dev/null &&\
  echo "file created" &&\
  mx native-image "$FILENAME" &&\
  echo "native image created" &&\
  "./${FILENAME,,}" >"substratevm_out$i.txt" 2>&1 &&\
  echo "native image executed" &&\
  $HOTSPOT_JAVA_HOME/bin/java "$FILENAME" >"hotspot_out$i.txt" 2>&1 &&\
  echo "HotSpot version executed" &&\
  ! diff "substratevm_out$i.txt" "hotspot_out$i.txt" >"comp_out$i.txt" 2>&1
done
