import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class App {

    private static final String KEY_CONSUMER_KEY = "consumer_key";
    private static final String KEY_CONSUMER_SECRET = "consumer_secret";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ACCESS_TOKEN_SECRET = "access_token_secret";
    private static final String KEY_SHEET_ID = "sheet_id";
    private static final String KEY_COLUMN_NAME = "column_name";
    private static final String KEY_COLUMN_FOLLOWERS_COUNT = "column_followers_count";
    private static final String KEY_QUEUE_COUNT= "queue_count";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage : java ConfigFile");
            return;
        }
        String fileName = args[0];
        Properties prop = new Properties();
        InputStream inputStream;
        String CONSUMER_KEY, CONSUMER_SECRET, ACCESS_TOKEN, ACCESS_TOKEN_SECRET, SHEET_ID, COLUMN_NAME, COLUMN_FOLLOWERS_COUNT, queueCountStr;
       int QUEUE_COUNT;

        try {
            inputStream = new FileInputStream(fileName);
            prop.load(inputStream);
            CONSUMER_KEY = prop.getProperty(KEY_CONSUMER_KEY);
            CONSUMER_SECRET = prop.getProperty(KEY_CONSUMER_SECRET);
            ACCESS_TOKEN = prop.getProperty(KEY_ACCESS_TOKEN);
            ACCESS_TOKEN_SECRET = prop.getProperty(KEY_ACCESS_TOKEN_SECRET);
            SHEET_ID = prop.getProperty(KEY_SHEET_ID);
            COLUMN_NAME = prop.getProperty(KEY_COLUMN_NAME);
            COLUMN_FOLLOWERS_COUNT = prop.getProperty(KEY_COLUMN_FOLLOWERS_COUNT);
                queueCountStr = prop.getProperty(KEY_QUEUE_COUNT);
            if (isNullOrEmpty(CONSUMER_KEY) || isNullOrEmpty(CONSUMER_SECRET)
                    || isNullOrEmpty(ACCESS_TOKEN) || isNullOrEmpty(ACCESS_TOKEN_SECRET)
            ||isNullOrEmpty(SHEET_ID) || isNullOrEmpty(COLUMN_NAME) || isNullOrEmpty(COLUMN_FOLLOWERS_COUNT)
                    || isNullOrEmpty(queueCountStr))
                    {
                throw new PropertyException("Invalid property");
            }
            QUEUE_COUNT = Integer.valueOf(queueCountStr);
            if(QUEUE_COUNT <1 || QUEUE_COUNT > 10){
                throw new PropertyException(KEY_QUEUE_COUNT + " cannot be less that 1 and greater than 10");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find file : " + fileName);
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        } catch (PropertyException e) {
            System.err.println(e.getMessage()+
                    "\nEnsure configuration properties are of the format\n\t" +
                    KEY_CONSUMER_KEY + "=value\n\t" +
                    KEY_CONSUMER_SECRET + "=value\n\t" +
                    KEY_ACCESS_TOKEN + "=value\n\t" +
                    KEY_ACCESS_TOKEN_SECRET + "=value\n\t" +
                    KEY_SHEET_ID+"=value=\n\t" +
                    KEY_COLUMN_NAME+"=value\n\t" +
                    KEY_COLUMN_FOLLOWERS_COUNT +"=value\n\t" );
            return;
        }

        try {
            GoogleSheetService.configureSheet(SHEET_ID, COLUMN_NAME, COLUMN_FOLLOWERS_COUNT, QUEUE_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(new StatusListener() {
            public void onException(Exception e) {

            }

            public void onStatus(Status status) {
                User user = status.getUser();
                if(1000 <= user.getFollowersCount() && user.getFollowersCount()<=5000){
                    System.out.println(user.getScreenName() + " : " +user.getFollowersCount());
                    try {
                        GoogleSheetService.writeToGoogleSheet(user.getScreenName(), String.valueOf(user.getFollowersCount()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            public void onTrackLimitationNotice(int i) {

            }

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {

            }
        });

        String[] hashTags = getHashTags();
        System.out.print("Filter stream based on the following hashTags: [");
        for (int i=0; i < hashTags.length-1; i++) {
            System.out.print(hashTags[i] + ", ");
        }
        System.out.println(hashTags[hashTags.length-1]+"]");
        FilterQuery tweetFilterQuery = new FilterQuery();
        tweetFilterQuery.track(hashTags);
        tweetFilterQuery.language(new String[]{"en"});
        twitterStream.filter(tweetFilterQuery);
    }

    private static String[] getHashTags() {
        Scanner scanner = new Scanner(System.in);
        String hashTags;
        do {
            System.out.println("Enter Each HashTag separated by comma or space");
            hashTags = scanner.nextLine();
        } while (isNullOrEmpty(hashTags));
        return getFormattedHashTags(hashTags);
    }

    private static String[] getFormattedHashTags(String hashtags) {
        hashtags = hashtags.replaceAll("\\s+", "#");
        hashtags = hashtags.replaceAll(",", "#");
        hashtags = hashtags.replaceAll("#+", " #");
        hashtags = hashtags.trim();
        String[] formattedHashtags = hashtags.split(" ");
        if(formattedHashtags[0].charAt(0) != '#') {
            formattedHashtags[0] = "#" + formattedHashtags[0];
        }
        return formattedHashtags;
    }

    private static boolean isNullOrEmpty(String s) {
        return  s == null || s.trim().equals("");
    }

    static class PropertyException extends Exception {
         PropertyException(String message) {
            super(message);
        }
    }
}
