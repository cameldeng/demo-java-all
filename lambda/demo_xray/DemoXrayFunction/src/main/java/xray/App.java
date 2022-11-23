package xray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import xray.resources.ResourceServiceImpl;
import xray.resources.model.Customer;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Map<String,Object>, Object> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Object handleRequest(final Map<String,Object> event, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        String body = event.get("body").toString();
        System.out.println("event Info: " + body);
        try {
            Customer queryCust = gson.fromJson(body,Customer.class);
            Customer customer = ResourceServiceImpl.getItemFromDynamoDB(queryCust);
            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            String output = String.format("customer info: %s \n input info: %s", gson.toJson(customer), body);
            return new GatewayResponse(output, headers, 200);
        } catch (IOException e) {
            return new GatewayResponse("{}", headers, 500);
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
