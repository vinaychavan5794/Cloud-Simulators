package com.vchava2;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class Broker3 extends DatacenterBrokerSimple {


    private List<CustomCloudlet2> cloudletList=null;
    public static String SIMULATION = "simulation1";
    public static Config config =  ConfigFactory.load(SIMULATION +".conf");
    int startID=0;


    /*This method allocates a cloudlet to a particular data center
    * based on the additional information provided with the cloudlet */

    public void submitTaskList(List<? extends CustomCloudlet2> list, List<NetworkDatacenter>networkDatacenterList) {
        cloudletList.addAll(list);
        for(CustomCloudlet2 customCloudlet2:cloudletList){
            Vm vm;
            if(startID==49){
                startID=0;
            }

            if(!customCloudlet2.getOs().equals("") || customCloudlet2.getCloudletSchedulerAbstract()!=null ){
                customCloudlet2.assignToDatacenter(networkDatacenterList.get(0));
                customCloudlet2.setNetworkDatacenter(networkDatacenterList.get(0));

                 vm=createVMImpl(startID,customCloudlet2.getCloudletSchedulerAbstract(),(int)customCloudlet2.getLength(),
                        (int)customCloudlet2.getNumberOfPes());
                 networkDatacenterList.get(0).getCharacteristics().
                         setOs(customCloudlet2.getOs());

            }else if(customCloudlet2.getVmSchedulerAbstract()!=null){
                customCloudlet2.assignToDatacenter(networkDatacenterList.get(1));
                customCloudlet2.setNetworkDatacenter(networkDatacenterList.get(1));
                vm=createVMImpl(startID,null,(int)customCloudlet2.getLength(),
                        0);
                networkDatacenterList.get(1).getHost(0).
                        setVmScheduler(customCloudlet2.getVmSchedulerAbstract());

                vm.setHost(networkDatacenterList.get(1).getHost(0));
                vm.setRam(customCloudlet2.getRam());

            }else{
                customCloudlet2.assignToDatacenter(networkDatacenterList.get(2));
                customCloudlet2.setNetworkDatacenter(networkDatacenterList.get(2));
                vm=createVMImpl(startID,null,(int)customCloudlet2.getLength(),
                        (int)customCloudlet2.getNumberOfPes());
            }

            super.submitVm(vm);
            super.submitCloudlet(customCloudlet2);
            startID++;

        }

    }

    public Broker3(CloudSim simulation, String name) throws Exception {
        super(simulation,name);
        cloudletList=new ArrayList<>();

    }

    /*This method reads the VM parameters from the config file, creates a VM and returns it*/

    public static Vm  createVMImpl(int  startId, CloudletSchedulerAbstract cloudletSchedulerAbstract,int mipSecond,int pesNumber){

        int mips = config.getInt(SIMULATION +".vm.mips");
        int imageSize = config.getInt(SIMULATION +".vm.imageSize");
        int pesNum = config.getInt(SIMULATION +".vm.pesNum");
        String vmm = config.getString(SIMULATION +".vm.vmm");
        int ram = config.getInt(SIMULATION +".vm.ram");
        int bw = config.getInt(SIMULATION +".vm.bw");
        Vm vm;
        if(cloudletSchedulerAbstract!=null){
            vm = new VmSimple( mipSecond*5, pesNumber,cloudletSchedulerAbstract);
        }else if (pesNumber==0){
            vm = new VmSimple( mipSecond*5, pesNum,new CloudletSchedulerTimeShared());
        }else {
            vm = new VmSimple( mipSecond*5, pesNumber,new CloudletSchedulerTimeShared());
        }

        vm.setId(startId);
        vm.setRam(ram);
        vm.setBw(bw);
        vm.setSize(imageSize);
        vm.setDescription(vmm);
        LOGGER.info("VM"+startId+" (mips="+mips+",size="+imageSize+",ram="+ram+",bw="+bw+",numCPUS="+pesNum+") created");

        return vm;
    }


}
