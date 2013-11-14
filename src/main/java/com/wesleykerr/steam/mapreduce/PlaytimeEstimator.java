package com.wesleykerr.steam.mapreduce;

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.steam.domain.player.GameStats;
import com.wesleykerr.steam.domain.player.Player;

/**
 * Estimates the 25% and 75% quantiles of each app so that
 * we can compute the mean and standard deviation and remove outliers.
 * 
 * Estimates based on the answer from: 
 * http://stackoverflow.com/questions/1058813/on-line-iterator-algorithms-for-estimating-statistical-median-mode-skewnes
 * 
 * @author wkerr
 *
 */
public class PlaytimeEstimator extends Configured implements Tool {
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaytimeEstimator.class);
    
    private static final double ETA = 0.001;
    private static final String DELIM = "\t";

    public static class PlaytimeEstimatorMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        private static final Logger LOGGER = LoggerFactory.getLogger(PlaytimeEstimatorMapper.class);
        
        private static Text KEY = new Text();
        private static DoubleWritable VALUE = new DoubleWritable();

        public void map(LongWritable key, Text value, Context context) 
                throws IOException, InterruptedException { 
            try { 
                LOGGER.info("KEY: " + key);
                String classValue = value.toString().split("\t")[1];
                Player p = gson.fromJson(classValue.toString(), Player.class);
                for (GameStats stats : p.getGames()) {
                    context.getCounter("Estimator", PlaytimeEstimatorMapper.class.getSimpleName()).increment(1);
                    if (stats.getCompletePlaytime() > 0) { 
                        KEY.set(stats.getAppid() + DELIM + "total");
                        VALUE.set(stats.getCompletePlaytime() / 60.0);
                        context.write(KEY, VALUE);
                    }
                    
                    if (stats.getRecentPlaytime() > 0) { 
                        KEY.set(stats.getAppid() + DELIM + "recent");
                        VALUE.set(stats.getRecentPlaytime() / 60.0);
                        context.write(KEY, VALUE);
                    }
                }
                
            } catch (JsonSyntaxException e) { 
                LOGGER.error("malformed json: " + value.toString());
                context.getCounter("Estimator", "Error JSON").increment(1);
            } catch (NumberFormatException nfe) {
                LOGGER.error("malformed json: " + value.toString());
                context.getCounter("Estimator", "Error NFE").increment(1);
            }
        }
    }
    
    public static class PlaytimeEstimatorReducer extends Reducer<Text, DoubleWritable, Text, Text> {
        private static Text VALUE = new Text();

        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException { 
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (DoubleWritable value : values) { 
                stats.addValue(value.get());
            }
            
            double quantile25 = stats.getPercentile(25);
            double median = stats.getPercentile(50);
            double quantile75 = stats.getPercentile(75);
            VALUE.set(quantile25 + DELIM + median + DELIM + quantile75);
            context.write(key, VALUE);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        
        Job job = new Job(conf, "Playtime Estimator");
        job.setJarByClass(PlaytimeEstimator.class);
        job.setMapperClass(PlaytimeEstimatorMapper.class);
        job.setReducerClass(PlaytimeEstimatorReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileSystem fs = FileSystem.get(new Configuration());
        fs.delete(new Path(otherArgs[1]), true);        
        LOGGER.debug("Deleted " + otherArgs[1]);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
    
    public static void main(String[] args) throws Exception { 
        System.exit(ToolRunner.run(new Configuration(), new PlaytimeEstimator(), args));
    }
}
