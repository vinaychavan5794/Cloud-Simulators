package com.vchava2;

import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.hosts.Host;


import java.util.List;

public class CustomCloudlet extends CloudletSimple {

    /**
     * Type enum allows us to specify two Cloudlet types: Mappers and Reducers.
     *
     * */
    public enum Type {
        MAPPER,
        REDUCER
    }

    private Type type = null;
    private Host host = null;

    // List of mappers that were reduced by the reducer
    private List<Integer> associatedMappersList = null;

    // Additional cost in case if there was a data transfer involved
    private Double processingDelay = null;

    public CustomCloudlet( long cloudletLength, int pesNumber) {
        super(cloudletLength,pesNumber);

    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Integer> getAssociatedMappersList() {
        return associatedMappersList;
    }

    public void setAssociatedMappersList(List<Integer> associatedMappersList) {
        this.associatedMappersList = associatedMappersList;
    }



    public Double getProcessingDelay() {
        return processingDelay;
    }

    public void setProcessingDelay(Double processingDelay) {
        this.processingDelay = processingDelay;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }




}
