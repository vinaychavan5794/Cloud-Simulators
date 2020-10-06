package com.vchava2;

import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;

import java.util.List;

public class CustomHost extends HostSimple {


    //Disk speed that needs to be considered in case of a data transfer between different hosts.
    private Double diskSpeed = 0.0;

    public CustomHost(long ram, long bw, long storage, List<? extends Pe> peList) {
        super( ram, bw, storage, (List<Pe>) peList);
    }

    public Double getDiskSpeed() {
        return diskSpeed;
    }

    public void setDiskSpeed(Double diskSpeed) {
        this.diskSpeed = diskSpeed;
    }
}
