package org.aws.demo.ssm;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemManagerImplTest {

    /**
     * get one parameter
     */
    @Test
    void testGetParameter_success() {
        String parameterName = "RDS-write-endpoint-ssm";
        ISystemManager systemManager = new SystemManagerImpl();
        String value = systemManager.getParameter(parameterName);
        System.out.println(String.format("Param %s has a value: %s", parameterName, value));
    }

    /**
     * get multi parameters
     */
    @Test
    void testGetParameters_success() {
        String[] paraNames = {"RDS-write-endpoint-ssm","RDS-read-endpoint-ssm","RDS-json-endpoints-ssm"};
        ISystemManager systemManager = new SystemManagerImpl();
        List<Parameter> values = systemManager.getParameters(paraNames);
        for (int i=0; i< values.size(); i++){
            Parameter parameter = values.get(i);
            System.out.println(String.format("Param %s has a value: %s \n", parameter.name(), parameter.value()));
        }
    }
}