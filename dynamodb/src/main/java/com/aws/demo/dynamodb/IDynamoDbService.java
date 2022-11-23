package com.aws.demo.dynamodb;

import com.aws.demo.dynamodb.model.Customer;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.Iterator;
import java.util.List;

public interface IDynamoDbService {

    /**
     * put record
     * @param customer entity
     * @return consumed capacity
     */
    Double putRecord(Customer customer);

    /**
     * query items by local secondary index or table
     * @param partitionKey partition key
     * @return items
     */
     Iterator<Page<Customer>> queryIndex (String partitionKey);

    /**
     * get consumed capacity unit for a query Index operation
     * @param partitionKey
     * @param sortKey
     * @return capacity Unit
     */
     Double getConsumedCapacityForQueryIndex(String partitionKey, String sortKey);

     /**
     * Scan table，返回数据外，并返回所消耗的RCU数量
     * @param attributeName 查询字段
     * @param attributeValue 字段值
     * @param limit 限制scan的数据量
     */
    public void scanTabel (String attributeName, String attributeValue, int limit);

    /**
     * query items by PartiQL
     * @param partitionKey partition key
     */
     void queryIndexByPartiQL(String partitionKey);

    /**
     * create table
     * @param tableName
     * @param partitionKey
     * @param sortKey
     * @return new table name
     */
    String creareTable (String tableName, String partitionKey, String sortKey);

    /**
     * batch write customer to table
     * @param customers
     */
    void batchWriteItem (List<Customer> customers );

    /**
     * parallel scan
     * @param tableName
     * @param itemLimit
     * @param numberOfThreads
     */
    void parallelScan (String tableName, int itemLimit, int numberOfThreads);
}
