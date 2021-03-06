/*
 * Copyright 2016 Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.inria.parallelSketchApproach;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>
 */
public class FParSketch {

    public static Configuration config;

    public static int[][] getRandomCombination(byte GroupSize, int NbrOfRandomVectors, short partitions) {

        Random  rand           = new Random();
        int[][] matCombination = new int[(int) GroupSize][partitions];

        ArrayList<String> listofCom = new ArrayList<>();

        int i = 0;

        while(i < partitions) {

            int a = rand.nextInt(NbrOfRandomVectors);
            int b = rand.nextInt(NbrOfRandomVectors);

            if(!listofCom.contains(a + "," + b)) {
                matCombination[0][i] = a;
                matCombination[1][i] = b;

                listofCom.add(a + "," + b);

                i++;
            }

        }

        return matCombination;

    }

    public static void main(String[] args) throws ClassNotFoundException {

        if(args.length < 3) {
            System.err.
                              println("Usage: F-ParSketch <inputDataSetFile> <inputQueryFile> <outputFiles> [<configFile>]");
            System.exit(1);
        }

        SparkConf conf = new SparkConf().setAppName("ParSketch").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer").set("spark.kryo.registrationRequired", "true");

        conf.registerKryoClasses(new Class<?>[]{float[].class, boolean[][].class, boolean[].class, int[].class, int[][].class, fr.inria.parallelSketchApproach.Group.class, java.util.HashMap.class});

        JavaSparkContext sc = new JavaSparkContext(conf);

        if(args.length == 3) {
            config = new Configuration();
        } else {
            config = new Configuration(sc.textFile(args[2]).collect());
        }

        JavaPairRDD<Integer, float[]> rdd1 = sc.objectFile(args[0]).mapToPair(new PairFunction<Object, Integer, float[]>() {

            @Override
            public Tuple2<Integer, float[]> call(Object t) throws Exception {
                return (Tuple2<Integer, float[]>) t;
            }
        });

        int[][] RandomCombination = getRandomCombination(config.getGroupSize(), config.
                                                                                              getNbrOfRandomVectors(), (short) config.
                                                                                                                                             getPartitions());

        GridConstruction grids = new GridConstruction(config);

        grids.gridConstruction(rdd1, sc, RandomCombination);

        grids.displayGrid();

        JavaPairRDD<Integer, float[]> rdd2 = sc.objectFile(args[1]).mapToPair(new PairFunction<Object, Integer, float[]>() {

            @Override
            public Tuple2<Integer, float[]> call(Object t) throws Exception {
                return (Tuple2<Integer, float[]>) t;
            }
        });

        List<Tuple2<Integer, Integer>> l = grids.get(rdd2, sc).
                collect();

        for(Tuple2<Integer, Integer> l1 : l) {
            System.out.println("Query " + l1._1 + ", Time Series " + l1._2);
        }

        System.out.println(grids.groupsRDDs.toDebugString());
        sc.stop();

    }

}
