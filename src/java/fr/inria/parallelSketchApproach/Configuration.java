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

import java.io.Serializable;
import java.util.List;

/**
 * @author Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>
 */
public class Configuration implements Serializable {


    private float fraction;
    private short partitions;


    private boolean normalization;

    private int   timeSeriesLength;
    private short threshold;
    private short   wordLen;

    // Query config
    private byte k;

    //Group Size
    private byte GroupSize;
    private int  NbrOfRandomVectors;

    public Configuration() {
        this.fraction = 0.7f;
        this.partitions = 100;

        this.normalization = true;
        this.timeSeriesLength = 256;
        this.threshold = 1000;


        this.k = 10;

        this.GroupSize = 2;
        this.NbrOfRandomVectors = 60;

    }

    public Configuration(List<String> configList) {

    }

    /**
     * @return the fraction
     */
    public float getFraction() {
        return fraction;
    }

    /**
     * @return the GroupSize
     */
    public byte getGroupSize() {
        return GroupSize;
    }

    /**
     * @return the k
     */
    public byte getK() {
        return k;
    }

    /**
     * @return the NbrOfRandomVectors
     */
    public int getNbrOfRandomVectors() {
        return NbrOfRandomVectors;
    }

    /**
     * @return the threshold
     */
    public short getThreshold() {
        return threshold;
    }

    /**
     * @return the timeSeriesLength
     */
    public int getTimeSeriesLength() {
        return timeSeriesLength;
    }

    /**
     * @return the normalization
     */
    public boolean isNormalization() {
        return normalization;
    }

    /**
     * @return the wordLen
     */
    public int getWordLen() {
        return wordLen;
    }


    /**
     * @return the partitions
     */
    public int getPartitions() {
        return partitions;
    }

}
