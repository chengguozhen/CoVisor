#!/bin/sh

OVXHOME=`dirname $0`/..
OVX_JAR="${OVXHOME}/target/OpenVirteX.jar"


JVM_OPTS="-Xms512m -Xmx2g"
## If you want JaCoCo Code Coverage reports... uncomment line below
#JVM_OPTS="$JVM_OPTS -javaagent:${OVXHOME}/lib/jacocoagent.jar=dumponexit=true,output=file,destfile=${OVXHOME}/target/jacoco.exec"
JVM_OPTS="$JVM_OPTS -XX:+TieredCompilation"
JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops"
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:MaxInlineSize=8192 -XX:FreqInlineSize=8192" 
JVM_OPTS="$JVM_OPTS -XX:CompileThreshold=1500 -XX:PreBlockSpin=8" 

cd ${OVXHOME}
echo "Packaging OVX for you..."
rm -rf "target"
mvn package -Dmaven.test.skip=true > scripts/errors.txt
cd -

if [ ! -e ${OVX_JAR} ]; then
    echo "OVX_JAR still does not exist."
fi
#echo "Starting OpenVirteX..."
#java ${JVM_OPTS} -Dlog4j.configurationFile=${OVXHOME}/config/log4j2.xml -Djavax.net.ssl.keyStore=${OVXHOME}/config/sslStore -jar ${OVX_JAR} $@
