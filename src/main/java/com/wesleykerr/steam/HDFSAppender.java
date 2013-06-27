package com.wesleykerr.steam;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class HDFSAppender {

	private FileSystem fs;
	private FSDataOutputStream output;
	private PrintWriter writer;
	
	public HDFSAppender() { 
		
	}
	
	public void open(String filePath) throws IOException { 
		Configuration conf = new Configuration();
        String hadoopHome = System.getenv("HADOOP_CONF_DIR");
        if (hadoopHome != null) { 
            System.out.println("Environment Variable Found: " + hadoopHome);
            conf.addResource(new Path(hadoopHome + "/core-site.xml"));
        } else { 
            // if it is not set, try to guestimate where it would be.
            conf.addResource(new Path("/usr/local/Cellar/hadoop/1.1.2/libexec/conf/core-site.xml"));
        }
		fs = FileSystem.get(URI.create(filePath), conf);
//		boolean flag = Boolean.getBoolean(fs.getConf().get("dfs.support.append"));
//		if (!flag)
//			throw new RuntimeException("Cannot append - please set dfs.support.append");
		
		output = fs.append(new Path(filePath));
		writer = new PrintWriter(output);
	}
	
	public void close() throws IOException { 
		writer.close();
		output.close();
		fs.close();
	}
	
	public void append(String line) { 
		writer.append(line);
	}
	
	public static void main(String[] args) throws IOException {
		String file = "hdfs://localhost:8020/tmp/timmy.txt";
		HDFSAppender append = new HDFSAppender();
		append.open(file);
		append.append("timmy test 1");
		append.append("timmy test 2");
		append.append("timmy test 3");
		append.append("timmy test 4");
		append.close();
		
		append.open(file);
		append.append("timmy test 5");
		append.append("timmy test 6");
		append.append("timmy test 7");
		append.close();
	}
}
