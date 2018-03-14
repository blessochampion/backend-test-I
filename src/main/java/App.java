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
    private static final int TWITTER_FOLLOWER_LOWER_bOUND = 1000;
    private static final int TWITTER_FOLLOWER_UPPER_bOUND = 50000;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage : java App ConfigFile");
            return;
        }
        String fileName = args[0];
        Properties prop = new Properties();
        InputStream inputStream;
        String consumerKey, consumerSecret, accessToken, accessTokenSecret, sheetId, columnName, columnFollowersCount, queueCountStr;
       int queueCount;

        try {
            inputStream = new FileInputStream(fileName);
            prop.load(inputStream);
            consumerKey = prop.getProperty(KEY_CONSUMER_KEY);
            consumerSecret = prop.getProperty(KEY_CONSUMER_SECRET);
            accessToken = prop.getProperty(KEY_ACCESS_TOKEN);
            accessTokenSecret = prop.getProperty(KEY_ACCESS_TOKEN_SECRET);
            sheetId = prop.getProperty(KEY_SHEET_ID);
            columnName = prop.getProperty(KEY_COLUMN_NAME);
            columnFollowersCount = prop.getProperty(KEY_COLUMN_FOLLOWERS_COUNT);
                queueCountStr = prop.getProperty(KEY_QUEUE_COUNT);
            if (isNullOrEmpty(consumerKey) || isNullOrEmpty(consumerSecret)
                    || isNullOrEmpty(accessToken) || isNullOrEmpty(accessTokenSecret)
            ||isNullOrEmpty(sheetId) || isNullOrEmpty(columnName) || isNullOrEmpty(columnFollowersCount)
                    || isNullOrEmpty(queueCountStr))
                    {
                throw new PropertyException("Invalid property");
            }
            queueCount = Integer.valueOf(queueCountStr);
            if(queueCount <1 || queueCount > 10){
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
            GoogleSheetService.configureSheet(sheetId, columnName, columnFollowersCount, queueCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(new StatusListener() {
            public void onException(Exception e) {

            }

            public void onStatus(Status status) {
                User user = status.getUser();
                if(TWITTER_FOLLOWER_LOWER_bOUND <= user.getFollowersCount() && user.getFollowersCount()<=TWITTER_FOLLOWER_UPPER_bOUND){
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
