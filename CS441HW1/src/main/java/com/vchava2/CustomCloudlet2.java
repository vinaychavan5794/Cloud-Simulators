package com.vchava2;


import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerAbstract;


public class CustomCloudlet2 extends CloudletSimple {

    // For setting the OS required for the cloudlet execution
    private String os="";
    private NetworkDatacenter networkDatacenter=null;

    //For setting Ram capacity of VM
    private long ram=0;


    public long getRam() {
        return ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    //For setting the cloudletscheduler policy
    private CloudletSchedulerAbstract cloudletSchedulerAbstract=null;

    //For setting the VM scheduler inside Host
    private VmSchedulerAbstract vmSchedulerAbstract=null;

    public VmSchedulerAbstract getVmSchedulerAbstract() {
        return vmSchedulerAbstract;
    }

    public void setVmSchedulerAbstract(VmSchedulerAbstract vmSchedulerAbstract) {
        this.vmSchedulerAbstract = vmSchedulerAbstract;
    }

    public NetworkDatacenter getNetworkDatacenter() {
        return networkDatacenter;
    }

    public CloudletSchedulerAbstract getCloudletSchedulerAbstract() {
        return cloudletSchedulerAbstract;
    }

    public void setCloudletSchedulerAbstract(CloudletSchedulerAbstract cloudletSchedulerAbstract) {
        this.cloudletSchedulerAbstract = cloudletSchedulerAbstract;
    }

    public void setNetworkDatacenter(NetworkDatacenter networkDatacenter) {
        this.networkDatacenter = networkDatacenter;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }


    public CustomCloudlet2(long cloudletLength, int pesNumber) {
        super(cloudletLength,pesNumber);

    }


}
