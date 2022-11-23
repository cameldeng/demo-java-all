package com.aws.demo.dynamodb;

import com.aws.demo.dynamodb.model.Customer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class DynamoDbServiceImpleTest {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void putRecord_withResponse() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        Customer customer = new Customer();
        customer.setId("ID333");
        customer.setName("Carlos_333");
        customer.setAge(47);
        customer.setBirthday(LocalDate.parse("1986-03-11"));
        Double consumedCapacity = ddbService.putRecord(customer);
        System.out.println("Consumed Capacity Unit: " + consumedCapacity);
    }

    @Test
    void putRecord_concurrency() throws InterruptedException {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(18);
        for (int i=1001; i<1020; i++){
            int serialNum = i;
            //Double consumedCapacity = ddbService.putRecord(constructCustomer(serialNum));
            executorService.execute(() ->{
                System.out.println(String.format("%d thread, Process starting...", serialNum));
                Double consumedCapacity = ddbService.putRecord(constructCustomer(serialNum));
                System.out.println(String.format("serialNum %d, Consumed Capacity: %f", serialNum, consumedCapacity.doubleValue()));
                latch.countDown();
            });
        }
        latch.await();
    }

    /**
     * 构建随机Cutomer信息
     * @param serialNum customer ID 后缀
     * @return Customer
     */
    private static Customer constructCustomer(int serialNum) {
        Customer customer = new Customer();
        customer.setId("ID"+serialNum);
        customer.setName("Carlos_"+serialNum);
        customer.setAge(serialNum);
        customer.setBirthday(LocalDate.parse("1986-03-11"));
        return customer;
    }

    @Test
    void queryIndex_success() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        String partitionKey = "ID0";
        Iterator<Page<Customer>> customers = ddbService.queryIndex(partitionKey);
        int page = 1;
        while (customers.hasNext()){
            //Map<String, AttributeValue> lastKey = customers.next().lastEvaluatedKey();
            List<Customer> customerPage = customers.next().items();
            System.out.println(String.format("\nPage No. %d with %d item(s):\n %s", page, customerPage.size(),gson.toJson(customerPage)));
            page ++;
        }
    }

    @Test
    void queryIndexWithClient_returnConsumedCapacity() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        String partitionKey = "ID0";
        Double capacityUnit =  ddbService.getConsumedCapacityForQueryIndex(partitionKey,"0");
        System.out.println("consumed capacity unit: " + capacityUnit);
    }

    @Test
    void testQueryIndexByPartiQL() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        String partitionKey = "ID0";
        ddbService.queryIndexByPartiQL(partitionKey);
    }


    @Test
    void testCreareTable() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        String tableName = "orders";
        String partitionKey = "id";
        String sortKey = "name";
        String newTabel = ddbService.creareTable(tableName,partitionKey,sortKey);
        System.out.println("new Tabel: " + newTabel + " has created");
    }

    @Test
    void testBatchWriteItem() {
        List<Customer> customers = new ArrayList<>();
        Customer customer = new Customer();
        customer.setId("ID100");
        customer.setName("Carlos_100");
        customer.setAge(100);
        customer.setBirthday(LocalDate.parse("1976-03-11"));
        customers.add(customer);

        Customer customer_1 = new Customer();
        customer_1.setId("ID101");
        customer_1.setName("Carlos_101");
        customer_1.setAge(101);
        customer_1.setBirthday(LocalDate.parse("1976-03-11"));
        customers.add(customer_1);

        Customer customer_2 = new Customer();
        customer_2.setId("ID102");
        customer_2.setName("Carlos_102");
        customer_2.setAge(102);
        customer_2.setBirthday(LocalDate.parse("1976-03-11"));
        customers.add(customer_2);

        IDynamoDbService ddbService = new DynamoDbServiceImple();
        ddbService.batchWriteItem(customers);
    }

    @Test
    void testParallelScan() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        String tableName = "demo_customer";
        String partitionKey = "id";
        String sortKey = "name";
        ddbService.parallelScan(tableName,5, 3);
    }

    /**
     * Scan table, 返回数据以及消耗的RCU数量
     * Scan做全表扫描，消耗的RCU是扫描过的所有数据量总和 / 4KB 来计算，
     * 请加上limit参数以限制扫描数据量
     */
    @Test
    void testScanTabel_withCunsumedCapacity() {
        IDynamoDbService ddbService = new DynamoDbServiceImple();
        ddbService.scanTabel("id","ID0", 1000);
    }
}