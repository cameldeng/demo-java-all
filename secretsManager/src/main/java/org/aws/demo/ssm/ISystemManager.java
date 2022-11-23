package org.aws.demo.ssm;

import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.List;

public interface ISystemManager {

    /**
     * get parameter value from Systems Manager parameter store
     * @param paraName parameter name
     * @return parameter value
     */
    String getParameter(String paraName);

    /**
     * get multi parameter from SSM parameter store
     * @param paraNames array of parameter name
     * @return List of parameter
     */
    List<Parameter> getParameters(String[] paraNames);
}
