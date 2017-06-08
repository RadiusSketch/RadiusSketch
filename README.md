# F-ParSketch
Fully Parallel Sketches for Time Series Indexing in Massively Distributed Environments.

 Requirements 
------------
F-ParSketch works with [Apache Spark](http://spark.apache.org/). In order to run F-ParSketch and ParSketch you must download and install [Apache Spark 1.6.2](http://spark.apache.org/news/spark-1-6-2-released.html)
	
 Building
------------

The code is written in Java and We use maven to build it, Use the given [pom.xml](https://github.com/djamelinfo/F-ParSketch/blob/master/pom.xml) file to build an executable jar containing all the dependencies.
 Usage
 
 
 
------------

	 for F-ParSketch use FParSketch class :
	$SPARK_HOME/bin/spark-submit --class fr.inria.parallelSketchApproach.FParSketch  --master $MASTER ParallelSketchApproach-0.jar  <inputDataSetFile> <inputQueryFile> <outputFiles> [<configFile>]
   
	 for ParSketch use ParSketch class :
	$SPARK_HOME/bin/spark-submit --class fr.inria.parallelSketchApproach.ParSketch  --master $MASTER ParallelSketchApproach-0.jar  <inputDataSetFile> <inputQueryFile> <outputFiles> [<configFile>]
 
 

You can use [Random Walk Time Series Generator](https://github.com/djamelinfo/RandomWalk-TimeSeriesGenerator-On-Spark) to produce a set of randomWalk datasets.

 
