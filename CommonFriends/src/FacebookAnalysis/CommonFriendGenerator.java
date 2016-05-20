package FacebookAnalysis;

import org.apache.hadoop.*;

import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.apache.hadoop.mapreduce.Reducer.Context;
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


public class CommonFriendGenerator {

	public static class Map extends
			Mapper<LongWritable, Text, Text, Text> {

		String input[] = new String[2];
		int member;
		int friend;

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String p;
			String input[] = value.toString().split(" ");
			int list[]=new int[input.length-1];
			for (int i = 1; i < input.length; i++)
				list[i-1]=Integer.parseInt(input[i]);
			
			ListWritable l=new ListWritable(list);
			
			int member = Integer.parseInt(input[0].trim());
			
			for (int i = 0; i < list.length; i++) {
				if (member < list[i])
					p=member+"-"+list[i];
				else
					p=list[i]+"-"+member;				
				
				context.write(new Text(p), new Text(l.toString()));
			}
		}

	}


	public static class Reduce extends
			Reducer<Text, Text, Text, Text> {
		Text result=new Text();
		public void reduce(Text key, Iterable<Text> friends,
				Context context) throws IOException, InterruptedException {
			
			  String sum = ""; 
		      for (Text val : friends) 
		        sum += val.toString() + ",";
		      
		      String lists[]=sum.trim().split(",");
		      result.set(getCommon(lists,key.toString()));

		      context.write(key, result); // create a pair <keyword, number of occurences>
		    }
		}

	public static String getCommon(String a[],String s){
		String common="";
		String k[]=s.trim().split("-");
		String c[]=a[0].trim().split(" ");
		String d[]=a[1].trim().split(" ");
		
		for (String i : c)
			for (String j:d)
				if (i.equals(j)&&!i.equals(k[0])&&!i.equals(k[1]))
					common=common.trim()+" "+i;	
				
		return common.trim();
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

		Job job = new Job(conf, "CommonFriendGenerator");
		job.setJarByClass(CommonFriendGenerator.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class); 
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
