
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleSheetService {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "DevCenter TwitterBot";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");
    private static final String HEADER_RANGE_RULE = "Sheet1!A1:B";
    private static final String ROW_RANGE_RULE = "Sheet1!A:B";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;
    private static Sheets service;
    private static String sheetId;
    private static List<List<Object>> nameFollowersPairList;
    private static int queueCount;


    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(SheetsScopes.SPREADSHEETS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                GoogleSheetService.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Sheets API client service.
     *
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void configureSheet(String userSheetId, String name, String followersCount, int itemCount) throws IOException {
        service = getSheetsService();
        sheetId = userSheetId;
        queueCount = itemCount;
        nameFollowersPairList = new ArrayList<List<Object>>(queueCount);
        String[] valuesToWrite = new String[]{name, followersCount};
        ValueRange header = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.<Object>asList(valuesToWrite)));
        AppendValuesResponse response = service.spreadsheets().values()
                .append(sheetId, HEADER_RANGE_RULE, header)
                .setValueInputOption("RAW")
                .execute();
    }

    public static void writeToGoogleSheet(String name, String followers) throws IOException {
        if (service == null) {
            throw new RuntimeException("Unable to write to sheet, call Configure() to intialize Service");
        }
        String[] valuesToWrite = new String[]{name, followers};
        nameFollowersPairList.add(Arrays.<Object>asList(valuesToWrite));
        if(nameFollowersPairList.size() == queueCount) {
            ValueRange header = new ValueRange()
                    .setValues(nameFollowersPairList);
            service.spreadsheets().values()
                    .append(sheetId, ROW_RANGE_RULE, header)
                    .setValueInputOption("RAW")
                    .execute();
            nameFollowersPairList.clear();
            System.out.println( queueCount + " Items Written to sheets ");
        }
    }

}