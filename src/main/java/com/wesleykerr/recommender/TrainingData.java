package com.wesleykerr.recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wesleykerr.recommender.utils.Ratings.InferredRatings;
import com.wesleykerr.recommender.utils.Ratings.PlayedRatings;
import com.wesleykerr.recommender.utils.Ratings.RatingsGenerator;
import com.wesleykerr.steam.persistence.MySQL;
import com.wesleykerr.steam.persistence.dao.GamesDAO;
import com.wesleykerr.steam.persistence.sql.GamesDAOImpl;

/**
 * This class has different methods for processing training
 * data prior to learning a model from it.
 * @author wkerr
 *
 */
public class TrainingData {
    private static final Logger LOGGER = Logger.getLogger(TrainingData.class);

    public static void formatForCF(Reader inputReader, Writer outputWriter, RatingsGenerator generator) 
            throws Exception { 
        
        JsonParser parser = new JsonParser();
        try (BufferedReader input = new BufferedReader(inputReader);
                BufferedWriter output = new BufferedWriter(outputWriter)) { 
            
            SummaryStatistics ownedGames = new SummaryStatistics();
            SummaryStatistics ratedGames = new SummaryStatistics();
            
            int count = 0;
            
            while (input.ready()) { 
                String line = input.readLine();
                String[] tokens = line.split("\t");
                JsonObject obj = parser.parse(tokens[1]).getAsJsonObject();
                JsonArray games = obj.get("games").getAsJsonArray();
                JsonArray ratingsArray = new JsonArray();
                for (JsonElement gameElement : games) { 
                    JsonObject gameObj = gameElement.getAsJsonObject();
                    Double rating = generator.getRating(gameElement.getAsJsonObject());
                    if (rating == null)
                        continue;

                    JsonObject ratingObj = new JsonObject();
                    ratingObj.addProperty("item", gameObj.get("appid").getAsLong());
                    ratingObj.addProperty("rating", rating);
                    ratingsArray.add(ratingObj);
                }

                JsonObject outputObj = new JsonObject();
                outputObj.addProperty("userId", tokens[0]);
                outputObj.add("ratings", ratingsArray);
                output.write(outputObj.toString());
                output.write("\n");

                ownedGames.addValue(games.size());
                ratedGames.addValue(ratingsArray.size());
                
                ++count;
                if (count % 10000 == 0)
                    LOGGER.info("Processed " + count + " lines");
            }
            
            LOGGER.info("Mean # games owned: " + ownedGames.getMean());
            LOGGER.info("Mean # games rated: " + ratedGames.getMean());
            
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
 
    public static void sparseMatrix(Reader inputReader, Writer outputWriter, 
    		RatingsGenerator generator) throws IOException { 
    
    	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-YYYY");
    	String date = sdf.format(GregorianCalendar.getInstance().getTime());
    	 
    	JsonParser parser = new JsonParser();
    	File tmpFile = File.createTempFile("ratings", "mm");

    	int count = 0;
    	long rowCount = 0;
    	long ratingCount = 0;
    	Map<Long,Integer> gamesMap = Maps.newHashMap();

    	try (BufferedReader input = new BufferedReader(inputReader);
    			BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile))) { 
                
            while (input.ready()) { 
                String line = input.readLine();
                String[] tokens = line.split("\t");
                JsonObject obj = parser.parse(tokens[1]).getAsJsonObject();
                JsonArray games = obj.get("games").getAsJsonArray();

                boolean addedRating = false;
                for (JsonElement gameElement : games) { 
                    JsonObject gameObj = gameElement.getAsJsonObject();
                    Double rating = generator.getRating(gameElement.getAsJsonObject());
                    if (rating == null)
                        continue;

                    Long appId = gameObj.get("appid").getAsLong();
                    Integer colIndex = gamesMap.get(appId);
                    if (colIndex == null) { 
                    	colIndex = gamesMap.size()+1;
                    	gamesMap.put(appId, colIndex);
                    }
                    output.write((rowCount+1) + " " + colIndex + " " + rating + "\n");

                    ++ratingCount;
                    addedRating = true;
                }

                if (addedRating)
                	++rowCount;

                ++count;
                if (count % 10000 == 0)
                    LOGGER.info("Processed " + count + " lines");
            }

        } catch (Exception e) { 
            e.printStackTrace();
        }
    	
    	try (BufferedWriter output = new BufferedWriter(outputWriter)) { 
        	output.write("%%MatrixMarket matrix coordinate real general\n");
        	output.write("% Generated " + date + "\n");
        	output.write(rowCount + " " + gamesMap.size() + " " + ratingCount + "\n");
        	try (BufferedReader input = new BufferedReader(new FileReader(tmpFile))) { 
        		while (input.ready()) { 
        			output.write(input.readLine() + "\n");
        		}
        	}
    	}
    	
    	File mapping = new File("/Users/wkerr/data-analysis/game-mapping.tsv");
    	try (BufferedWriter output = new BufferedWriter(new FileWriter(mapping))) {
    		for (Map.Entry<Long,Integer> entry : gamesMap.entrySet()) { 
    			output.write(entry.getKey() + "\t" + entry.getValue() + "\n");
    		}
    	}
    }
    
    /**
     * 
     * @return
     */
    public static Options getOptions() { 
    	Options options = new Options();
    	
    	@SuppressWarnings("static-access")
		Option input = OptionBuilder
    			.withLongOpt("input")
    			.withArgName("input")
    			.hasArg()
    			.isRequired()
    			.create("i");
    	options.addOption(input);

    	@SuppressWarnings("static-access")
		Option output = OptionBuilder
    			.withLongOpt("output")
    			.withArgName("output")
    			.hasArg()
    			.isRequired()
    			.create("o");
    	options.addOption(output);

    	@SuppressWarnings("static-access")
    	Option sparse = OptionBuilder
    			.withLongOpt("sparse")
    			.withArgName("s")
    			.create("s");
    	options.addOption(sparse);

    	@SuppressWarnings("static-access")
    	Option help = OptionBuilder
    			.withLongOpt("help")
    			.withArgName("help")
    			.create("h");
    	options.addOption(help);

    	return options;
    }
    
    /**
     * 
     * @param options
     */
    public static void printHelp(Options options) { 
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp( "TrainingData", options );
    }
    
    public static void runJson(File inputFile, File outputFile) throws Exception { 
    	MySQL mysql = MySQL.getDreamhost();
        GamesDAO gamesDAO = new GamesDAOImpl(mysql.getConnection());
        
        RatingsGenerator rg = new InferredRatings(gamesDAO);
        try (InputStream inStream = new FileInputStream(inputFile);
                InputStream gzipInputStream = new GZIPInputStream(inStream);
                Reader reader = new InputStreamReader(gzipInputStream, "UTF-8");
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile));
                Writer writer = new OutputStreamWriter(gzipOutputStream, "UTF-8") ) { 
            formatForCF(reader, writer, rg);
        }
    }
    
    public static void runSparse(File inputFile, File outputFile) throws Exception { 
    	try (InputStream inStream = new FileInputStream(inputFile);
    			InputStream gzipInputStream = new GZIPInputStream(inStream);
    			Reader reader = new InputStreamReader(gzipInputStream, "UTF-8")) { 
    		sparseMatrix(reader, 
    			new FileWriter(outputFile), 
    			new PlayedRatings());
    	}
    }
    
    public static void main(String[] args) throws Exception { 
    	Options options = getOptions();
    	CommandLineParser parser = new BasicParser();

    	File inputFile = new File("/data/steam/training-data.gz");
    	File outputFile = new File("/data/steam/ratings-file.gz");
    	
    	boolean runSparse = false;
    	try {
    		CommandLine line = parser.parse(options, args);
        	inputFile = new File(line.getOptionValue("i"));
        	outputFile = new File(line.getOptionValue("o"));
        	
    		if (line.hasOption("h")) {
    			printHelp(options);
    			System.exit(1);
    		}
    		
    		if (line.hasOption("s"))
    			runSparse = true;
    	} catch (ParseException exp) { 
    		printHelp(options);
    		System.exit(1);
    	}

    	if (runSparse)
    		runSparse(inputFile, outputFile);
    	else
    		runJson(inputFile, outputFile);
    }
}
