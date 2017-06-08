package fr.inria.slidingWindow.SketchApproach;

import fr.inria.parallelSketchApproach.Configuration;
import fr.inria.parallelSketchApproach.RandomProjection;
import fr.inria.randomWalk.RandomWalk;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

/**
 * Created by djamel on 24/10/16.
 */
public class SketchSlidingWindow {


    public static Configuration config;

    public static void main(String[] args) throws ClassNotFoundException {

        String output               = "";
        String ListOfinputQueryFile = "";
        String configFile           = "";
        String input                = "";
        String configVal            = "";
        String A                    = "";
        long   numbTS               = 0;
        long   numbOfQuery          = 0;
        int    timeSeriesLength     = 0;
        int nbrSW = 0;

        int a = 1;

        if(args.length < 1) {
            System.err.println("Usage: SketchSlidingWindow [OPTION]...");
            System.out.println("Try 'SketchSlidingWindow -h' for more information.");
            System.exit(1);
        }


        for(int i = 0; i < args.length; i++) {
            System.out.println(i + " " + args[i]);
            switch(args[i]) {

                case "-f":
                    i++;
                    input = args[i];
                    break;
                case "-g":
                    i++;
                    numbTS = Long.parseLong(args[i]);
                    i++;
                    timeSeriesLength = Integer.parseInt(args[i]);
                    break;
                case "-G":
                    i++;
                    numbOfQuery = Integer.parseInt(args[i]);
                    break;
                case "-o":
                    i++;
                    output = args[i];
                    break;
                case "-q":
                    i++;
                    ListOfinputQueryFile = args[i];
                    break;
                case "-c":
                    i++;
                    configFile = args[i];
                    break;
                case "--config":
                    i++;
                    configVal = args[i];
                    break;
                case "-a":
                    i++;
                    a = Integer.parseInt(args[i]);
                    break;
                case "-A":
                    i++;
                    A = args[i];
                    break;

                case "-h":
                    System.out.println("Usage: SketchSlidingWindow [OPTION]...\n");
                    System.out.println("Options:");
                    System.out.println("  -f\t\t  Input file");
                    System.out.println("  -g\t\t  RandomWalk Time Series Generator, you must give number of Time Series and Number of elements in each Time Series.");
                    System.out.println("  -G\t\t  RandomWalk Query Time Series Generator, you must give number of Time Series and .");
                    System.out.println("  -o\t\t  Output directory.");
                    System.out.println("  -q\t\t  Comma-separated list of input query file.");
                    System.out.println("  -c\t\t  Path to a file from which to load extra properties. If not specified, this will use defaults propertie.");
                    System.out.println("  --config\t  Change given configuration variables, NAME=VALUE (e.g fraction, " + "partitions, " + "normalization, " + "wordLen, " + "timeSeriesLength, " + "threshold, " + "k)");
                    System.out.println("  -a\t\t  1, 2, 3");
                    System.out.println("  -A\t\t  1, 2");
                    System.out.println("  -h\t\t  Show this help message and exit.\n");
                    System.out.println("Full documentation at: <>");
                    System.exit(1);
                    break;

            }

        }


        SparkConf conf = new SparkConf().setAppName("ParSketch").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer").set("spark.kryo.registrationRequired", "true");

        conf.registerKryoClasses(new Class<?>[]{float[].class, boolean[][].class, boolean[].class, int[].class, int[][].class, fr.inria.parallelSketchApproach.Group.class, java.util.HashMap.class});

        JavaSparkContext sc = new JavaSparkContext(conf);


        if(configFile.equals("")) {

            config = new Configuration();

        } else {
            config = new Configuration(sc.textFile(configFile).collect());
        }

        //if(!configVal.equals("")) {
        //    config.changeConfig(configVal);
        //}

        JavaPairRDD<Integer, float[]> rdd1;

        if(!input.equals("")) {
            rdd1 = sc.objectFile(input).mapToPair(new PairFunction<Object, Integer, float[]>() {
                @Override
                public Tuple2<Integer, float[]> call(Object t) throws Exception {
                    return (Tuple2<Integer, float[]>) t;
                }
            });
        } else rdd1 = RandomWalk.GeneratedRandomWalkData(sc, numbTS, timeSeriesLength, config.getPartitions()).persist(StorageLevel.MEMORY_ONLY());


        for (int i = 0; i < nbrSW; i++) {
            
        }




        




    }

}
