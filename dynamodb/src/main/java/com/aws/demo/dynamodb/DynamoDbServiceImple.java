package com.aws.demo.dynamodb;

import com.aws.demo.dynamodb.model.Customer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DynamoDbServiceImple implements IDynamoDbService {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Region region = Region.US_EAST_1;

    @Override
    public Double putRecord(Customer customer) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);
        /** enhanced client **/
        DynamoDbEnhancedClient enhancedClient = DynamoDbClientFactory.getEnhancedClient(ddbClient);
        //build dynamodb table
        DynamoDbTable<Customer> custTable = enhancedClient.table("demo_customer", TableSchema.fromBean(Customer.class));

        customer.setCreateTime(LocalDateTime.now());
        customer.setModifyTime(LocalDateTime.now());
        //construct request
        PutItemEnhancedRequest request = PutItemEnhancedRequest.builder(Customer.class)
                        .item(customer)
                //NONE: no consumedCapacity; TOTAL: table + index consumed capacity; INDEX: table CU with index CU
                        .returnConsumedCapacity("TOTAL")
                        .build();
        //construct response
        PutItemEnhancedResponse response = custTable.putItemWithResponse(request);
        ddbClient.close();
        return response.consumedCapacity().capacityUnits();
    }

    @Override
    public Iterator<Page<Customer>> queryIndex (String partitionKey) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);
        DynamoDbEnhancedClient enhancedClient = DynamoDbClientFactory.getEnhancedClient(ddbClient);
        //build dynamodb table
        DynamoDbTable<Customer> custTable = enhancedClient.table("demo_customer", TableSchema.fromBean(Customer.class));

        //construct local secondary index
        DynamoDbIndex<Customer> secIndex = custTable.index("age-index");

        //create query parameter
        AttributeValue attributeValue = AttributeValue.builder()
                .s(partitionKey)
                .build();

        // Create a QueryConditional object that's used in the query operation.
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(attributeValue).build());

        QueryEnhancedRequest enhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                /** Trigger Fetch operation for attribute "birthday" from base table. **/
                .attributesToProject("id","name","age","birthday","createTime","modifyTime")
                .consistentRead(true)
                .limit(2)
                .build();

        // Get items in the table.
        SdkIterable<Page<Customer>> results = secIndex.query(enhancedRequest);


        return results.stream().iterator();
//        return customers.get();
    }

    @Override
    public Double getConsumedCapacityForQueryIndex(String partitionKey, String sortKey) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);

        // Set up an alias(#id) for the partition key name in case it's a reserved word.
        Map<String, String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#id","id");
        expressionAttributesNames.put("#age", "age");
        expressionAttributesNames.put("#name", "name");

        // Set up mapping of the partition name with the value.
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":idValue", AttributeValue.builder().s(partitionKey).build());
        expressionAttributeValues.put(":ageValue", AttributeValue.builder().n(sortKey).build());
        expressionAttributeValues.put(":nameValue", AttributeValue.builder().s("Carlos_14").build());

        QueryRequest request = QueryRequest.builder()
                .tableName("demo_customer")
                .indexName("age-index")
                .projectionExpression("id,#name,age,birthday")
                .keyConditionExpression("#id = :idValue AND #age >= :ageValue")
                .filterExpression("#name = :nameValue")
                .expressionAttributeNames(expressionAttributesNames)
                .expressionAttributeValues(expressionAttributeValues)
                .returnConsumedCapacity("INDEXES")
                .build();

        QueryResponse response = ddbClient.query(request);
        Double capacityUnit = response.consumedCapacity().capacityUnits();
        System.out.println("consumed capacity: " + capacityUnit);
        return capacityUnit;
    }

    /**
     * Scan table，返回数据外，并返回所消耗的RCU数量
     * @param attributeName 查询字段
     * @param attributeValue 字段值
     * @param limit 限制scan的数据量
     */
    public void scanTabel (String attributeName, String attributeValue, int limit) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);

        AttributeValue idValue = AttributeValue.builder()
                .s(attributeValue).build();

        Map<String,AttributeValue> expressAttrValues = new HashMap<>();
        expressAttrValues.put(":"+attributeName, idValue);

        ScanRequest request;
        if (limit > 0) {
            request  = ScanRequest.builder()
                    .tableName("demo_customer")
                    .filterExpression( attributeName + "= :"+attributeName)
                    .expressionAttributeValues(expressAttrValues)
                    .returnConsumedCapacity("INDEXES")
                    .limit(limit)
                    .build();
        } else {
            request  = ScanRequest.builder()
                    .tableName("demo_customer")
                    .filterExpression("id = :id")
                    .expressionAttributeValues(expressAttrValues)
                    .returnConsumedCapacity("INDEXES")
                    .build();
        }
        ScanResponse response = ddbClient.scan(request);
        System.out.println("Consumed Capacity Unit:" + response.consumedCapacity().capacityUnits());
        Iterator<Map<String,AttributeValue>> customerIterator = response.items().listIterator();
        int count = 0;
        while (customerIterator.hasNext()) {
            count++;
            Map<String, AttributeValue> item = customerIterator.next();
            System.out.println(String.format("item %d: id=%s, name=%s",
                    count, item.get("id").s(), item.get("name").s()));
        }
    }

    @Override
    public void queryIndexByPartiQL(String partitionKey) {
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);

        String sqlStatement = "SELECT id,age FROM \"demo_customer\".\"age-index\" where id = ? ORDER BY age";
        List<AttributeValue> parameters = new ArrayList<>();
        AttributeValue idValue = AttributeValue.builder()
                .s("ID0")
                .build();
        parameters.add(idValue);
        //create request
        ExecuteStatementRequest request = ExecuteStatementRequest.builder()
                .statement(sqlStatement)
                .parameters(parameters)
                .returnConsumedCapacity("INDEXES")
                .build();

        //execute request
        ExecuteStatementResponse response = ddbClient.executeStatement(request);
        System.out.println("consumed capacity unit: " + response.consumedCapacity());
        System.out.println("ExecuteStatement successful: \n"+ response.items().toString());
    }

    @Override
    public String creareTable (String tableName, String partitionKey, String sortKey) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);

        //declare partition key and sort key datatype
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(AttributeDefinition.builder().attributeName(partitionKey).attributeType(ScalarAttributeType.S).build());
        attributeDefinitions.add(AttributeDefinition.builder().attributeName(sortKey).attributeType(ScalarAttributeType.S).build());

        //declare partition key and sort key
        List<KeySchemaElement> keySchemas = new ArrayList<>();
        keySchemas.add(KeySchemaElement.builder().attributeName(partitionKey).keyType(KeyType.HASH).build());
        keySchemas.add(KeySchemaElement.builder().attributeName(sortKey).keyType(KeyType.RANGE).build());

        //set up provision throughput on table
        ProvisionedThroughput throughput = ProvisionedThroughput.builder()
                .readCapacityUnits(5L)
                .writeCapacityUnits(5L)
                .build();

        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(attributeDefinitions)
                .tableName(tableName)
                .keySchema(keySchemas)
                .provisionedThroughput(throughput)
                .build();

        CreateTableResponse response = ddbClient.createTable(request);

        /** use waiter get table description **/
        DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();
        DynamoDbWaiter waiter = ddbClient.waiter();
        WaiterResponse<DescribeTableResponse> waiterResponse = waiter.waitUntilTableExists(describeTableRequest);
        waiterResponse.matched().response().ifPresent(System.out::println);
        String newTable = response.tableDescription().tableName();
        System.out.println("Table status: " +response.tableDescription().tableStatusAsString());

        return newTable;
    }

    @Override
    public void batchWriteItem (List<Customer> customers ) {
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);
        DynamoDbEnhancedClient enhancedClient = DynamoDbClientFactory.getEnhancedClient(ddbClient);
        //build dynamodb table
        DynamoDbTable<Customer> custTable = enhancedClient.table("demo_customer", TableSchema.fromBean(Customer.class));

        WriteBatch writeBatch = WriteBatch.builder(Customer.class)
                .mappedTableResource(custTable)
                .addPutItem(customers.get(0))
                .addPutItem(customers.get(1))
                .addPutItem(customers.get(2))
                .build();
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBatch)
                .build();

        BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
        System.out.println("Result : " + result.toString());
    }

    @Override
    public void parallelScan (String tableName, int itemLimit, int numberOfThreads){
        //build DynamoDbEnhancedClient
        DynamoDbClient ddbClient = DynamoDbClientFactory.getDynamoDbClient(region);
        DynamoDbEnhancedClient enhancedClient = DynamoDbClientFactory.getEnhancedClient(ddbClient);
        //build dynamodb table
        DynamoDbTable<Customer> custTable = enhancedClient.table("demo_customer", TableSchema.fromBean(Customer.class));

        //Iterator<Customer> customerIterator = custTable.scan().items().iterator();
        System.out.println(
                "Scanning " + tableName + " using " + numberOfThreads + " threads " + itemLimit + " items at a time");

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // Divide DynamoDB table into logical segments
        // Create one task for scanning each segment
        // Each thread will be scanning one segment
        int totalSegements = numberOfThreads;

        for (int segment=0; segment < totalSegements; segment ++){
            ScanSegmentTask task = new ScanSegmentTask(custTable, tableName,itemLimit, totalSegements,segment);
            executorService.execute(task);
        }
        shutDownExecutorService(executorService);

    }

    private static class ScanSegmentTask implements Runnable {
        // DynamoDB table
        private DynamoDbTable<Customer> custTable;
        // DynamoDB table to scan
        private String tableName;

        // number of items each scan request should return
        private int itemLimit;

        // Total number of segments
        // Equals to total number of threads scanning the table in parallel
        private int totalSegments;

        // Segment that will be scanned with by this task
        private int segment;

        public ScanSegmentTask(DynamoDbTable<Customer> custTable, String tableName, int itemLimit, int totalSegments, int segment) {
            this.custTable = custTable;
            this.tableName = tableName;
            this.itemLimit = itemLimit;
            this.totalSegments = totalSegments;
            this.segment = segment;
        }

        @Override
        public void run() {
            System.out.println("Scanning " + tableName + " segment " + segment + " out of " + totalSegments
                    + " segments " + itemLimit + " items at a time...");
            int totalScannedItemCount = 0;

            try {
                ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                        .segment(segment)
                        .totalSegments(totalSegments)
                        .limit(itemLimit)
                        .build();

                Iterator<Customer> customerIterator = custTable.scan(request).items().iterator();

                Customer customer = null;
                while (customerIterator.hasNext()) {
                    totalScannedItemCount++;
                    customer = customerIterator.next();
                    System.out.println("segment + " + segment + " result: " +customer.getName());
                }

            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
            finally {
                System.out.println("Scanned " + totalScannedItemCount + " items from segment " + segment + " out of "
                        + totalSegments + " of " + tableName);
            }
        }
    }

    private static void shutDownExecutorService(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
