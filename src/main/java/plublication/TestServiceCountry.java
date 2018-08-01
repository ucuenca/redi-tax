/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package plublication;

import com.jayway.jsonpath.JsonPath;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 *
 * @author joe
 */
public class TestServiceCountry {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
       String Country = URLEncoder.encode("Ecua","UTF-8").replace("+","%20");  
       
       System.out.println ( "VALOR "+ get ("https://restcountries.eu/rest/v2/name/"+Country , "$.[0].alpha2Code")+" ");
    }
    
     public static String get(String query , String path) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(query);
            HttpResponse response = client.execute(request);
 
            int responseCode = response.getStatusLine().getStatusCode();
 
            System.out.println("**GET** request Url: " + request.getURI());
            System.out.println("Response Code: " + responseCode);
            System.out.println("Content:-\n");
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 
            String line = "";
            while ((line = rd.readLine()) != null) {
                return JsonPath.read(line, path);
            }
           
        } catch (ClientProtocolException e) {
            System.out.print (e);
        } catch (UnsupportedOperationException | IOException e) {
            System.out.print (e);
        }
        return null;
    }
    
}
