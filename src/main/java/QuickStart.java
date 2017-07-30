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
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.cloud.pubsub.v1.AckReplyConsumer;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class QuickStart {
	
	private static String projectId = "extreme-braid-174817";
	private static String subscriptionId = "avvv1";

	static int [] arr = new int[5];
	int i = 0;
	
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Google Sheets API Java QuickStart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
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
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            QuickStart.class.getResourceAsStream("/client_secret.json");
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
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // Build a new authorized API client service.
        Sheets service = getSheetsService();

        String spreadsheetId = "1eFXr5HC6W9QbGl0GQBPbn9-XWcFzrgTrvgWuhwd6fp8";
        String range = "A1:5";
        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            System.out.println("No data found.");
        } else {
          for (List row : values) {
            // Print columns A and E, which correspond to indices 0 and 4.
            System.out.printf("%s,\n", row.get(0));
          }
        }
	updateSpreadSheet();
	
	 while (true)
	 {
		System.out.println("before");
		displayStatus();
		SubscriptionName subscriptionName = SubscriptionName.create(projectId, subscriptionId);
	// Instantiate an asynchronous message receiver
		MessageReceiver receiver = new MessageReceiver() {
      @Override
      public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        // handle incoming message, then ack/nack the received message
        System.out.println("Id : " + message.getMessageId());
        System.out.println("Data : " + message.getData().toStringUtf8());
		String[] word = message.getData().toStringUtf8().split("\\s");
		int index = Integer.parseInt(word[0]);
		int value = Integer.parseInt(word[1]);
		if (index < arr.length)
			arr[index] = value;
        consumer.ack();
      }
    };
	
	Subscriber subscriber = null;
	subscriber = Subscriber.defaultBuilder(subscriptionName, receiver).build();
	subscriber.startAsync();
	
	System.out.println("after");
	   displayStatus();
	 }
	}
	
	public static void updateSpreadSheet() throws IOException
	{
		String	range = "A2"; // TODO: Update placeholder value.
		String spreadsheetId = "1eFXr5HC6W9QbGl0GQBPbn9-XWcFzrgTrvgWuhwd6fp8";

		String valueInputOption = "USER_ENTERED";

		Object[] arr = new Object[3];
		arr[0] = "3";
		arr[1] = "8";
		List<List<Object>> values1 = Arrays.asList(
        Arrays.asList(arr
            )
        // Additional rows ...
        ); 
		ValueRange requestBody = new ValueRange();
		requestBody.setValues(values1);
		Sheets sheetsService = getSheetsService();
		Sheets.Spreadsheets.Values.Update request =
        sheetsService.spreadsheets().values().update(spreadsheetId, range, requestBody);
		request.setValueInputOption(valueInputOption);

		UpdateValuesResponse response1 = request.execute();

		System.out.println(response1);
	}
	
	public static void displayStatus() throws InterruptedException
	{
		Thread.sleep(1000);
		for (int i = 0; i < 5; i++)
			System.out.println(i);
	}
}