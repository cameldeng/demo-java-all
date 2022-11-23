package xray.resources;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import xray.resources.model.Customer;

import java.util.Map;

public class ResourceServiceImpl {
    private final static Region region = Region.US_EAST_1;

    public static Customer getItemFromDynamoDB(Customer queryCust) {
        AWSXRay.beginSubsegment("# building DynamoDBClient begin");
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);
        DynamoDbEnhancedClient enhancedClient = DynamoDbClientFactory.getEnhancedClient(ddbClient);
        //build dynamodb table
        DynamoDbTable<Customer> custTable = enhancedClient.table("demo_customer", TableSchema.fromBean(Customer.class));

        Key key = Key.builder().partitionValue(queryCust.getId()).sortValue(queryCust.getName()).build();

        GetItemEnhancedRequest enhancedRequest = GetItemEnhancedRequest.builder()
                .key(key)
                .build();
        AWSXRay.endSubsegment();

        AWSXRay.beginSubsegment("# get item from DynamoDB");
        Customer customer = custTable.getItem(enhancedRequest);
        AWSXRay.endSubsegment();
        return customer;
    }
}
