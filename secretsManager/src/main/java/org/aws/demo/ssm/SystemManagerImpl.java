package org.aws.demo.ssm;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.List;

public class SystemManagerImpl implements ISystemManager {

    /**
     * get parameter from System Manager parameter store
     */
    @Override
    public String getParameter(String paraName) {
        SsmClient ssmClient = SsmClient.builder()
                .region(Region.US_EAST_1)
                .build();

        GetParameterRequest request = GetParameterRequest.builder()
                .name(paraName)
                .build();

        GetParameterResponse response = ssmClient.getParameter(request);
        return response.parameter().value();
    }

    @Override
    public List<Parameter> getParameters(String[] paraNames) {
        SsmClient ssmClient = SsmClient.builder()
                .region(Region.US_EAST_1)
                .build();
        GetParametersRequest request = GetParametersRequest.builder()
                .names(paraNames)
                .build();

        GetParametersResponse response = ssmClient.getParameters(request);
        return response.parameters();
    }
}
