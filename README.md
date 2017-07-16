# TF-IDF-Spark-Scala-implementation
This is implementation of [TF-IDF algorithm](https://en.wikipedia.org/wiki/Tf–idf) on Scala and Spark.

Input - folder with text files. Each row of each file contains id and a text of a document separeted by "\t".

Output - folder with text files. Each row of each file contains word and a list of 20 pairs of document id and TF-IDF score which are the most relevant for this word.

The project is focused on efficiency and scalability thus text preprocessing is kept very simple. Punctuation and digits are removed, all text coerced to lover case. At the same time opportunities for further development are embedded in the system.

The project already has several optimizations:
1. Amount of shuffles is reduced to 1.
2. BufferTopHolder is introduced for efficient aggregation of inverted index.
3. All distributed variables are created in memory saving way.
4. Kryo serialization is used instead of Java serialization.

What else can be done:
1. Additional tuning after data exploration (cluster resources, partitions, data scew).
2. Changing input/output format (avro, parquet, sequence, msgpack, gzip) for faster reading from disc.
3. Native optimizations of Spark SQL (Catalyst and Tungsten modules).
4. TF-IDF in Spark ML or Spark MLlib.

The project should be compiled with gradle.
```
./gradlew assemble
```
This will produce a fat jar (uber jar) with all required dependencies except Spark and Scala which should be provided.

Job should be launched with spark-submit.
```
export HADOOP_CONF_DIR=/etc/hadoop/conf
export HADOOP_USER_NAME=hdfs

spark-submit \ 
--class TfIdfJob \
--deploy-mode cluster \
--master yarn \
tf-idf-job/target/libs/tf-idf-job-0.0.1-SNAPSHOT.jar \
--inputFolder:/example/textCorpus \
--outputFolder:/example/invertedIndex
```
