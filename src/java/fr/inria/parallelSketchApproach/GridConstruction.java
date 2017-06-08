/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.parallelSketchApproach;

import org.apache.spark.HashPartitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>
 */
public class GridConstruction implements Serializable {

    Broadcast<boolean[][]>      RandomVectors;
    Broadcast<int[][]>          RandomCombination;
    JavaPairRDD<Integer, Group> groupsRDDs;
    private Configuration config;

    public GridConstruction(Configuration config) {
        this.config = config;
    }

    public static int[][] chunkArray(int[] array, int chunkSize) {
        int     numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
        int[][] output      = new int[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start  = i * chunkSize;
            int length = ((array.length - start) <= chunkSize) ? (array.length - start) : chunkSize;

            int[] temp = new int[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }

    public static boolean generatRandomPositiveNegitiveValue() {

        int ii = -1 + (int) (Math.random() * ((1 - (-1)) + 1));
        return (ii >= 0);

    }

    // (myBoolean) ? 1 : 0;
    public static boolean[][] getRandomVectors(int numberOfVectors, int sizeOfVectors) {

        boolean[][] vectors = new boolean[sizeOfVectors][numberOfVectors];

        for(int j = 0; j < numberOfVectors; j++) {

            for(int i = 0; i < sizeOfVectors; i++) {
                vectors[i][j] = generatRandomPositiveNegitiveValue();
            }

        }

        return vectors;

    }

    public void gridConstruction(JavaPairRDD<Integer, float[]> TimeSeriesRDDs, JavaSparkContext sc, int[][] randomCombination) {

        RandomVectors = sc.broadcast(getRandomVectors(config.getNbrOfRandomVectors(), config.getTimeSeriesLength()));
        RandomCombination = sc.broadcast(randomCombination);

        groupsRDDs = TimeSeriesRDDs.flatMapToPair(new RandomProjection(RandomVectors, RandomCombination)).partitionBy(new HashPartitioner(config.getPartitions()));

    }

    public void displayGrid() {
        List<Tuple2<Integer, Group>> l = groupsRDDs.collect();

        for(Tuple2<Integer, Group> l1 : l) {
            System.out.println(l1);
        }
    }

    public JavaPairRDD<Integer, Integer> get(JavaPairRDD<Integer, float[]> TimeSeriesRDDs, JavaSparkContext sc) {

        JavaPairRDD<String, Integer> querysRDDs = TimeSeriesRDDs.flatMapToPair(new RandomProjection(RandomVectors, RandomCombination)).
                partitionBy(new HashPartitioner(config.getPartitions())).mapToPair(new PairFunction<Tuple2<Integer, Group>, String, Integer>() {

                                                                            @Override
                                                                            public Tuple2<String, Integer> call(Tuple2<Integer, Group> t) throws Exception {
                                                                                return new Tuple2<>(((int) t._2.s1) + "" + ((int) t._2.s1), t._2.ID);
                                                                            }
                                                                        });

        final HashMap<String, Integer> mp = new HashMap<>();

        mp.putAll(querysRDDs.collectAsMap());

        final Broadcast<HashMap<String, Integer>> q = sc.broadcast(mp);

        JavaPairRDD<Integer, Integer> r = groupsRDDs.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer, Group>, Integer, Integer>() {

            @Override
            public Iterable<Tuple2<Integer, Integer>> call(Tuple2<Integer, Group> t) throws Exception {
                List<Tuple2<Integer, Integer>> l = new ArrayList<>();

                String str = ((int) t._2.s1) + "" + ((int) t._2.s1);

                if(q.value().containsKey(str)) {
                    l.add(new Tuple2<>(q.value().get(str), t._2.ID));
                }

                return l;

            }
        }).groupByKey().flatMapToPair(new PairFlatMapFunction<Tuple2<Integer, Iterable<Integer>>, Integer, Integer>() {

            @Override
            public Iterable<Tuple2<Integer, Integer>> call(Tuple2<Integer, Iterable<Integer>> t) throws Exception {
                HashMap<Integer, Integer> mp = new HashMap<>();
                Iterable<Integer>         i  = t._2;
                if(i != null) {
                    for(Integer i1 : i) {
                        if(i1 != null) {
                            if(!mp.containsKey(i1)) {
                                mp.put(i1, 1);
                            } else {
                                mp.put(i1, mp.get(i1) + 1);
                            }
                        }

                    }
                }

                List<Tuple2<Integer, Integer>> l = new ArrayList<>();

                for(Map.Entry<Integer, Integer> entrySet : mp.entrySet()) {
                    Integer key   = entrySet.getKey();
                    Integer value = entrySet.getValue();

                    if(value > 1) {
                        l.add(new Tuple2<>(t._1, key));
                    }

                }

                return l;
            }
        });

        return r;

    }

    public void saveAsTextFile(String path) {
        groupsRDDs.saveAsTextFile(path);
    }

}
