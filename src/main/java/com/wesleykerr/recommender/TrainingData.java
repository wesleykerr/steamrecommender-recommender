package com.wesleykerr.recommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wesleykerr.recommender.utils.Ratings.InferredRatings;
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
    
    public static void main(String[] args) throws Exception { 
        MySQL mysql = MySQL.getDreamhost();
        GamesDAO gamesDAO = new GamesDAOImpl(mysql.getConnection());
        
        RatingsGenerator rg = new InferredRatings(gamesDAO);
        File inputFile = new File("/data/steam/training-data.gz");
        File outputFile = new File("/data/steam/ratings-file.gz");
        try (InputStream inStream = new FileInputStream(inputFile);
                InputStream gzipInputStream = new GZIPInputStream(inStream);
                Reader reader = new InputStreamReader(gzipInputStream, "UTF-8");
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outputFile));
                Writer writer = new OutputStreamWriter(gzipOutputStream, "UTF-8") ) { 
            formatForCF(reader, writer, rg);
            
        }
    }
}
