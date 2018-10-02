#!/bin/bash

PREFIX="MyGeneratedClazz"
NFILES=$1
shift

echo "SVM_JAVA_HOME=$SVM_JAVA_HOME"
echo "HOTSPOT_JAVA_HOME=$HOTSPOT_JAVA_HOME"
echo "Generating $NFILES class files"
for i in `seq 1 $NFILES`; do
  echo "Generating class file"
  FILENAME="$PREFIX$i"
  if $SVM_JAVA_HOME/bin/java -jar jbgenerator.jar -filename "$FILENAME" "$@" >/dev/null 2>"jbgenerator.$i.err.txt"; then
    echo "Class file \"$FILENAME.class\" created"
    echo "Generating native image"
    if mx native-image "$FILENAME" >/dev/null 2>"svm.$i.err.txt"; then
      echo "Native image \"${FILENAME,,}\" created" &&\

      SVM_OUT="svm.$i.out.txt"
      HOTSPOT_OUT="hotspot.$i.out.txt"
      DIFF_OUT="diff.$i.txt"

      $HOTSPOT_JAVA_HOME/bin/java "$FILENAME" >"$HOTSPOT_OUT" 2>&1
      echo "Class file executed"

      "./${FILENAME,,}" >"$SVM_OUT" 2>&1
      echo "Native image executed"

      if ! diff "$SVM_OUT" "$HOTSPOT_OUT"; then
        echo "Different outcome in file \"$FILENAME.class\""
        diff "$SVM_OUT" "$HOTSPOT_OUT" >"$DIFF_OUT" 2>&1;
      fi
    fi
  fi
done
