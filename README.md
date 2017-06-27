# TF-IDF-Spark-Scala-implementation
This is production ready implementation of [TF-IDF algorithm](https://en.wikipedia.org/wiki/Tf–idf) on Scala and Spark.

Input - folder with text files. Each row of each file contains id and a text of a document separeted by "\t".

Output - folder with text file. Each row of each file contains word and a list of 20 document id, TF-IDF score pairs most relevant for this word.

The project is focused on efficiency and scalability thus text preprocessing is kept very simple. Punctuation and digits are removed, all text coerced to lover case. At the same time opportunities for further development are embedded in the system.

The project should be compiled with gradle.
```
./gradlew assemble
```
This will produce a fat jar (uber jar) with all dependencies required except Spark which should be provided.

Job should be launched with spark-submit.
```
export HADOOP_CONF_DIR=/etc/hadoop/conf
export HADOOP_USER_NAME=hdfs

spark-submit \ 
--class \
--deploy-mode cluster \
--master yarn \
--queue default \
--num-executors 5 \
--executor-memory 3G \
--driver-memory 1G \
--conf spark.kryoserializer.buffer.max=300m \
target/libs/.jar \
--inputFolder:/user/example/textCorpus \
--outputFolder:/user/example/invertedIndex
```
