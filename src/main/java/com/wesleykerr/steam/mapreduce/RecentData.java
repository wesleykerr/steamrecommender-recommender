package com.wesleykerr.steam.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wesleykerr.steam.domain.player.Player;

public class RecentData extends Configured implements Tool {
	private static final Gson gson = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(RecentDataMapper.class);

	public static class RecentDataMapper extends Mapper<LongWritable, Text, Text, Text> {
		private static final Logger LOGGER = LoggerFactory.getLogger(RecentDataMapper.class);
		
		public void map(LongWritable key, Text value, Context context) 
				throws IOException, InterruptedException { 
			try { 
				Player p = gson.fromJson((String) value.toString(), Player.class);
				if (p.isVisible() && !p.getGames().isEmpty()) 
					context.write(new Text(p.getId()), value);
			} catch (JsonSyntaxException e) { 
				LOGGER.error("malformed json: " + value.toString());
			} catch (NumberFormatException nfe) {
				LOGGER.error("malformed json: " + value.toString());
			}
		}
	}
	
	public static class RecentDataReducer extends Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException { 
			
			long maxTime = Long.MIN_VALUE;
			String json = null;
			
			for (Text value : values) { 
				Player p = gson.fromJson((String) value.toString(), Player.class);
				if (p.getUpdateDateTime() > maxTime) {
					maxTime = p.getUpdateDateTime();
					json = value.toString();
				}
			}
			if (json != null)  
				context.write(key, new Text(json));
		}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		Job job = new Job(conf, "Recent Data");
		job.setJarByClass(RecentData.class);
		job.setMapperClass(RecentDataMapper.class);
		job.setReducerClass(RecentDataReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileSystem fs = FileSystem.get(new Configuration());
		fs.delete(new Path(otherArgs[1]), true);		
		LOGGER.debug("Deleted " + otherArgs[1]);

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		FileOutputFormat.setCompressOutput(job, true);
		FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}
	
	public static void main(String[] args) throws Exception { 
		System.exit(ToolRunner.run(new Configuration(), new RecentData(), args));
	}

}
