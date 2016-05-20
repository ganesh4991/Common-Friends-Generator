package FacebookAnalysis;

import org.apache.hadoop.*;

import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;
import java.text.DecimalFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.util.LineReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import org.apache.commons.cli.*;

public class InputProcessing {

	public static class Map extends
			Mapper<LongWritable, Text, IntWritable, IntWritable> {

		String input[] = new String[2];
		int member;
		int friend;

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			input = value.toString().split("\n");
			int l = 0;
			while (l < input.length) {

				String content[] = input[l].split(" ");
				context.write(new IntWritable(Integer.parseInt(content[0])),
						new IntWritable(Integer.parseInt(content[1])));
				context.write(new IntWritable(Integer.parseInt(content[1])),
						new IntWritable(Integer.parseInt(content[0])));
				l++;
			}
		}

	}


	public static class Reduce extends
			Reducer<IntWritable, IntWritable, IntWritable, ListWritable> {

		public void reduce(IntWritable key, Iterable<IntWritable> friends,
				Context context) throws IOException, InterruptedException {

			List<Integer> list=new ArrayList<Integer>();
			for (IntWritable a : friends)
				list.add(a.get());

			ListWritable l=new ListWritable(list);
			
			context.write(key, l);
		}

	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Usage: InputProcessing <input-path> <out-path>");
			System.exit(2);
		}

		Job job = new Job(conf, "InputProcessing");
		job.setJarByClass(InputProcessing.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ListWritable.class);

		job.setInputFormatClass(NLinesInputFormat.class); // Custom Input for
															// which enables map
															// to process 100
															// lines in a single
															// call.
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
