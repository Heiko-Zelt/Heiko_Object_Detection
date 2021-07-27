unset CLASSPATH
export JAVA_HOME=/usr/lib/jvm/java-1.14.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
java -cp ~/git-repos/Heiko_GMAF_Plugin/GMAF_Plugin_Project/lib/GMAF-jar-with-dependencies.jar de.swa.tools.SampleEvaluation result.xml annotations.csv

