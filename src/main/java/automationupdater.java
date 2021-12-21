import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;



public class automationupdater {
    public static List<ArrayList> parsedAttributes = new ArrayList<>(6);


    public static void main(String args[]) throws IOException, ParseException, InterruptedException {
        jsonDownloader();
        updateDatabase();
    }

    public static void jsonParser(String unparsedJson) {
        String[] parsedOriginal = unparsedJson.split("\\{", 0);
        parsedOriginal = ArrayUtils.remove(parsedOriginal, 0);


        for (int i = 0; i < 6; i++) {
            parsedAttributes.add(new ArrayList());
        }
        List<String> tempStorage = new ArrayList<>();


        for (int i = 0; i < parsedOriginal.length; i++) {
            tempStorage.add(parsedOriginal[i]);
        }

        for (int i = 0; i < tempStorage.size(); i++) {
            String[] temp = tempStorage.get(i).split(",", 0);
            if (temp[0].substring(6, temp[0].length() - 1).contains("ios")) {
                parsedAttributes.get(0).add("iOS");
            } else if (temp[0].substring(6, temp[0].length() - 1).contains("android")) {
                parsedAttributes.get(0).add("Android");
            } else {
                parsedAttributes.get(0).add(temp[0].substring(6, temp[0].length() - 1));
            }

            parsedAttributes.get(1).add(temp[1].substring(14, temp[1].length() - 1));
            parsedAttributes.get(2).add(temp[2].substring(11, temp[2].length() - 1));


            if ((temp[3].substring(9, temp[3].length()).contains("null"))) {
                parsedAttributes.get(3).add(temp[3].substring(9, temp[3].length()));
            } else {
                parsedAttributes.get(3).add(temp[3].substring(10, temp[3].length() - 1));
            }

            if ((temp[4].substring(18, temp[4].length())).contains("null")) {
                parsedAttributes.get(4).add(temp[4].substring(18, temp[4].length()));
            } else {
                parsedAttributes.get(4).add(temp[4].substring(19, temp[4].length() - 1));
            }

            if (i != parsedOriginal.length - 1) {
                parsedAttributes.get(5).add(temp[5].substring(14, temp[5].length() - 1));
            } else {
                parsedAttributes.get(5).add(temp[5].substring(14, temp[5].length() - 2));
            }

        }
    }

    public static void updateDatabase() {
        try{
            Connection con = DriverManager.getConnection(
                    "INSERT YOUR MYSQL SERVER PATH HERE");

            PreparedStatement stmt = con.prepareStatement("truncate browsers");
            stmt.execute();


            for (int k=0; k < parsedAttributes.get(0).size(); k++)
            {

                 String statement ="INSERT INTO `browsers`(`os`, `os_version`, `browser`, `device`, `browser_version`, `real_mobile`) VALUES (";
                for (int l = 0; l < 6; l++)
                {
                    statement+= '"';
                    statement+= parsedAttributes.get(l).get(k);
                    statement+= '"';

                    if (l != 5){
                    statement+= ",";
                    }
                    else {
                        statement+= ");";
                }

                }
                stmt.addBatch(statement);

            }
            stmt.executeBatch();


        }catch(SQLException ex){         System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }


    public static String jsonDownloader() throws IOException, InterruptedException, ParseException {
        final String username = "ADD YOUR BROWSERSTACK USERNAME HERE";
        final String password = "cADD YOUR BROWSERSTACK ACCESS KEY HERE";
        String plainCredentials = username + ":" + password;
        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes(StandardCharsets.UTF_8)));
        String authorizationHeader = "Basic " + base64Credentials;
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.browserstack.com/automate/browsers.json"))
                .GET()
                .header("Authorization", authorizationHeader)
                .header("Content-Type", "application/json")
                .build();
        // Send HTTP request
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        jsonParser(response.body());
        return response.body();
    }
}
