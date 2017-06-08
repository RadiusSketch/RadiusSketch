package fr.inria.slidingWindow.SketchApproach;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;
import scala.reflect.ClassTag;

/**
 * Created by djamel on 10/05/17.
 */
public class GridRDD extends JavaPairRDD {


    public GridRDD(RDD<Tuple2> rdd, ClassTag classTag, ClassTag classTag2) {
        super(rdd, classTag, classTag2);
    }
}

