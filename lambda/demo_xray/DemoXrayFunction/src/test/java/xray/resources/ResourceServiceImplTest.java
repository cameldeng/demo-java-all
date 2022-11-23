package xray.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import xray.resources.model.Customer;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServiceImplTest {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    void getItemFromDynamoDB() {
        Customer queryCust = new Customer();
        queryCust.setId("ID0");
        queryCust.setName("Carlos_0");
        Customer customer = ResourceServiceImpl.getItemFromDynamoDB(queryCust);
        System.out.println("customer Info: " + gson.toJson(customer));
    }
}