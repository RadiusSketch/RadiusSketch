/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.parallelSketchApproach;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>
 */
public class RandomProjection implements PairFlatMapFunction<Tuple2<Integer, float[]>, Integer, Group> {

    private final boolean[][] randomVectors;
    private final int[][]     matCombination;

    public RandomProjection(Broadcast<boolean[][]> randomVectors, Broadcast<int[][]> matCombination) {
        this.matCombination = matCombination.getValue();
        this.randomVectors = randomVectors.getValue();

    }

    private short boolToByate(boolean b) {
        return (short) ((b) ? 1 : -1);
    }

    @Override
    public Iterable<Tuple2<Integer, Group>> call(Tuple2<Integer, float[]> t) throws Exception {

        float[] skatch = new float[randomVectors.length];

        List<Tuple2<Integer, Group>> list = new ArrayList<>();

        float temp;

        for(int i = 0; i < randomVectors.length; i++) {
            temp = 0;
            for(int j = 0; j < randomVectors[0].length; j++) {
                temp += boolToByate(randomVectors[i][j]) * t._2[j];
            }

            skatch[i] = (float) (temp * 0.5);
        }

        for(int i = 0; i < this.matCombination[0].length; i++) {
            list.add(new Tuple2<>(i, new Group(skatch[this.matCombination[0][i]], skatch[this.matCombination[1][i]], t._1)));
        }

        return list;

    }

}
