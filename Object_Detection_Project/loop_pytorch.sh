unset CLASSPATH
export JAVA_HOME=/usr/lib/jvm/java-1.14.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
for i in 1 2 3 4 5 6 7 8 9; do
java -Dfile.encoding=UTF-8 -classpath /home/heiko/git-repos/Heiko_Object_Detection/Object_Detection_Project/target/classes:/home/heiko/.m2/repository/ai/djl/api/0.11.0/api-0.11.0.jar:/home/heiko/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar:/home/heiko/.m2/repository/net/java/dev/jna/jna/5.3.0/jna-5.3.0.jar:/home/heiko/.m2/repository/org/apache/commons/commons-compress/1.20/commons-compress-1.20.jar:/home/heiko/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar:/home/heiko/.m2/repository/ai/djl/basicdataset/0.11.0/basicdataset-0.11.0.jar:/home/heiko/.m2/repository/org/apache/commons/commons-csv/1.8/commons-csv-1.8.jar:/home/heiko/.m2/repository/ai/djl/model-zoo/0.11.0/model-zoo-0.11.0.jar:/home/heiko/.m2/repository/ai/djl/pytorch/pytorch-model-zoo/0.11.0/pytorch-model-zoo-0.11.0.jar:/home/heiko/.m2/repository/ai/djl/pytorch/pytorch-engine/0.11.0/pytorch-engine-0.11.0.jar:/home/heiko/.m2/repository/ai/djl/pytorch/pytorch-native-auto/1.8.1/pytorch-native-auto-1.8.1.jar:/home/heiko/.m2/repository/org/apache/logging/log4j/log4j-api/2.14.1/log4j-api-2.14.1.jar:/home/heiko/.m2/repository/org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar -XX:+ShowCodeDetailsInExceptionMessages de.heikozelt.objectdetection.BatchJob collection pytorch_result$i.xml 0.$i
done
