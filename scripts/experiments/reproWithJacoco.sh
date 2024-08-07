#!/bin/bash
# Figure out script absolute path
pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

ROOT_DIR=`dirname $SCRIPT_DIR`
ROOT_DIR=`dirname $ROOT_DIR`

if [ "$#" -lt 5 ]; then
  echo "Usage: $0 [-i] CLASS_SUFFIX TEST_METHOD JACOCO_OUTPUT_FILE JACOCO_INCLUDE_GLOB TEST_FILE" >&2
  exit 1
fi

args=""
if [ "$1" = "-i" ]; then
  args="-i"
  shift 1
fi

JACOCO_SOURCES=$ROOT_DIR/examples/target/dependency-sources
if [ ! -d $JACOCO_SOURCES ]; then
  (cd $ROOT_DIR/examples && mvn -q dependency:unpack-dependencies -Dclassifier=sources -DincludeArtifactIds=maven-model,closure-compiler,rhino,ant,bcel -DoutputDirectory=target/dependency-sources)
fi

class="$1"
method="$2"
JACOCO_JAR=$ROOT_DIR/target/jacocoagent.jar
if [ ! -f $JACOCO_JAR ]; then
  mvn -q dependency:copy -Dartifact=org.jacoco:org.jacoco.agent:0.8.7 -DoutputDirectory=$ROOT_DIR/target/
  (cd $ROOT_DIR/target && unzip org.jacoco.agent-0.8.7.jar)
fi

if [ -f $ROOT_DIR/examples/target/dependency/org.jacoco.report-0.8.10.jar ]; then
  rm $ROOT_DIR/examples/target/dependency/org.jacoco.report-0.8.10.jar
  mvn -q dependency:get -Dartifact=org.jacoco:org.jacoco.report:0.8.7
  mvn -q dependency:copy -Dartifact=org.jacoco:org.jacoco.report:0.8.7 -DoutputDirectory=$ROOT_DIR/examples/target/dependency/
fi
if [ -f $ROOT_DIR/examples/target/dependency/org.jacoco.core-0.8.10.jar ]; then
  rm $ROOT_DIR/examples/target/dependency/org.jacoco.core-0.8.10.jar
  mvn -q dependency:get -Dartifact=org.jacoco:org.jacoco.core:0.8.7
  mvn -q dependency:copy -Dartifact=org.jacoco:org.jacoco.core:0.8.7 -DoutputDirectory=$ROOT_DIR/examples/target/dependency/
fi
echo $JACOCO_JAR
# Chocopy contains a copy of ant, so we need to exclude it when processing coverage for ant...
rm -f $ROOT_DIR/examples/target/dependency/chocopy*

export CLASSPATH="$ROOT_DIR/examples/target/classes/:$ROOT_DIR/examples/target/test-classes/:$ROOT_DIR/examples/target/dependency/*"
export JVM_OPTS="-javaagent:$JACOCO_JAR=destfile=$3,includes=$4"

"$ROOT_DIR/bin/jqf-repro" $args "$class" "$method" "${@:5}"
echo "$ROOT_DIR/bin/jqf-repro" $args "$class" "$method" "${@:5}"
