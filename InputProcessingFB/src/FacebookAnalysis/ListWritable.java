package FacebookAnalysis;

import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;

//Custom Writable class which stores user pairs
class ListWritable implements Writable {
	Set<IntWritable> set;

	public ListWritable() {
		set = new TreeSet<IntWritable>();
	}
	public ListWritable(List<Integer> a) {
		set = new TreeSet<IntWritable>();
		for (Integer i:a)
			set.add(new IntWritable(i));
	}
	public ListWritable(int a[]) {
		set = new TreeSet<IntWritable>();
		for (int i:a)
			set.add(new IntWritable(i));
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		for (IntWritable a : set)
			a.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		for (IntWritable a : set)
			a.write(out);
	}
	
	@Override
	public String toString(){
		String s="";
		for (IntWritable a : set)
			s=s+" "+a.get();
		return s;
	}
	
}