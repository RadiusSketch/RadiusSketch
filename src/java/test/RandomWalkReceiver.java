package test;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by djamel on 24/10/16.
 */
public class RandomWalkReceiver extends Receiver<Pairs> {


    private int nbrOfTimeseries = 0;

    public RandomWalkReceiver(int nbrOfTimeseries) {
        super(StorageLevel.MEMORY_AND_DISK());
        this.nbrOfTimeseries = nbrOfTimeseries;
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.err.println("Usage: randomWalkReceiver <nbrOfTimeseries>");
            System.exit(1);
        }

        SparkConf sparkConf = new SparkConf().setAppName("JavaCustomReceiver").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer").set("spark.kryo.registrationRequired", "true");
        ;

        //@formatter:off
        sparkConf.registerKryoClasses(new Class<?>[]{scala.collection.immutable.Range.class,
                                                     Class.forName("scala.collection.mutable.WrappedArray$ofRef"),
                                                     org.apache.spark.streaming.receiver.Receiver[].class,
                                                     Class.forName("test.RandomWalkReceiver"),
                                                     Class.forName("test.Pairs"),
                                                     float[].class,
                                                        Class.forName("scala.collection.mutable.ArrayBuffer"),
                                                     Object[].class});

        //@formatter:on
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, new Duration(1000));

        JavaReceiverInputDStream<Pairs> lines = ssc.receiverStream(new RandomWalkReceiver(Integer.valueOf(args[0])));

        lines.mapToPair(new PairFunction<Pairs, Integer, Float>() {
            @Override
            public Tuple2<Integer, Float> call(Pairs pairs) throws Exception {
                return new Tuple2<Integer, Float>(pairs.getId(), pairs.getValADouble());
            }
        }).groupByKeyAndWindow(new Duration(10000)).mapToPair(new PairFunction<Tuple2<Integer,
                Iterable<Float>>,
                Integer, Float[]>() {
            @Override
            public Tuple2<Integer, Float[]> call(Tuple2<Integer, Iterable<Float>> integerIterableTuple2) throws Exception {
                Iterator<Float> it   = integerIterableTuple2._2().iterator();
                List<Float>     list = new ArrayList<>();
                while(it.hasNext()) {
                    list.add(it.next());
                }
                Float[] ts = new Float[list.size()];
                return new Tuple2<Integer, Float[]>(integerIterableTuple2._1(), list.toArray(ts));
            }
        }).map(new Function<Tuple2<Integer, Float[]>, java.lang.String>() {
            @Override
            public java.lang.String call(Tuple2<Integer, Float[]> v1) throws Exception {
                return v1._1() + "," + v1._2().length;
            }
        }).print();


        ssc.start();
        ssc.awaitTermination();

    }

    public static double[][] chunkArray(double[] array, int chunkSize) {
        int        numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
        double[][] output      = new double[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start  = i * chunkSize;
            int length = ((array.length - start) <= chunkSize) ? (array.length - start) : chunkSize;

            double[] temp = new double[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }

    public static double Mean(double[] data, int index1, int index2) throws Exception {

        if(index1 < 0 || index2 < 0 || index1 >= data.length || index2 >= data.length) {
            throw new Exception("Invalid index!");
        }

        if(index1 > index2) {
            int temp = index2;
            index2 = index1;
            index1 = temp;
        }

        double sum = 0;

        for(int i = index1; i <= index2; i++) {
            sum += data[i];
        }

        return sum / (index2 - index1);
    }

    public static double StdDev(double[] timeSeries) throws Exception {
        double mean = Mean(timeSeries, 1, timeSeries.length - 1);
        double var  = 0.0;

        for(int i = 1; i < timeSeries.length; i++) {
            var += (timeSeries[i] - mean) * (timeSeries[i] - mean);
        }
        var /= (timeSeries.length - 2);

        return Math.sqrt(var);
    }

    public static double[] Z_Normalization(double[] timeSeries) throws Exception {
        double mean = Mean(timeSeries, 1, timeSeries.length - 1);
        double std  = StdDev(timeSeries);

        double[] normalized = new double[timeSeries.length];

        if(std == 0) {
            std = 1;
        }

        for(int i = 1; i < timeSeries.length - 1; i++) {
            normalized[i] = (timeSeries[i] - mean) / std;
        }

        return normalized;
    }

    public static float[] randomWalk(int length, float startPoint) {
        NormalDistribution n  = new NormalDistribution(new JDKRandomGenerator(), 0, 1); //mean 0 std 1 variance 1
        float[]            ts = new float[length + 1];
        ts[0] = startPoint;

        for(int i = 1; i < ts.length; i++) {
            ts[i] = ts[i - 1] + (float) n.sample();
        }
        return ts;
    }

    @Override
    public void onStart() {

        // Start the thread that receives data over a connection
        new Thread() {
            @Override
            public void run() {
                receive();
            }
        }.start();

    }

    @Override
    public void onStop() {

    }


    private void receive() {


        float[] startPoint = new float[nbrOfTimeseries];

        NormalDistribution n = new NormalDistribution(new JDKRandomGenerator(), 0, 1);


        try {


            while(!isStopped()) {
                ArrayList<Pairs> list = new ArrayList<>();

                long start = System.currentTimeMillis();

                for(int i = 0; i < startPoint.length; i++) {
                    startPoint[i] += n.sample();
                    list.add(new Pairs(i, startPoint[i]));
                }

                System.out.println((System.currentTimeMillis() - start) + "");

                store(list.iterator());

                long slip = 1000 - (System.currentTimeMillis() - start);

                if(slip > 0) Thread.sleep(slip);


            }
        } catch(Exception e) {

            e.printStackTrace();

        }

    }

    private void receive1() {

        int     timeSeriesSize = 1;
        float[] startPoint     = new float[nbrOfTimeseries];


        try {

            while(!isStopped()) {
                ArrayList<float[]> list = new ArrayList<>();

                long start = System.currentTimeMillis();

                for(int i = 0; i < startPoint.length; i++) {
                    float[] ts = randomWalk(timeSeriesSize, startPoint[i]);
                    ts[0] = i;
                    startPoint[i] = ts[ts.length - 1];
                    list.add(ts);
                }

                System.out.println((System.currentTimeMillis() - start) + "");

                //store(list.iterator());

                long slip = 1000 - (System.currentTimeMillis() - start);

                if(slip > 0) Thread.sleep(slip);


            }
        } catch(Exception e) {

            e.printStackTrace();

        }

    }
}
