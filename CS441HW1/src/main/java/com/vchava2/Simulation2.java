package com.vchava2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Simulation2  {

    public static String SIMULATION = "simulation2";
    public static Config config =  ConfigFactory.load(SIMULATION +".conf");
    public static Logger LOGGER = LoggerFactory.getLogger(Simulation2.class);
    public static CloudSim cloudSim;
    public static String space = "    ";
    public static DecimalFormat decimalFormat = new DecimalFormat("###.##");


    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        LOGGER.debug("Starting Simulation ..");

        int numberVms = config.getInt(SIMULATION +".numberVms");
        int numberDcs = config.getInt(SIMULATION +".numberDcs");
        int numberMappers = config.getInt(SIMULATION +".numberMappers");
        int numberReducers = config.getInt(SIMULATION +".numberReducers");
        cloudSim = new CloudSim();

        List<NetworkDatacenter> networkDatacenterList = createDatacenterList(numberDcs,0,new ArrayList<>());

        for(NetworkDatacenter networkDatacenter:networkDatacenterList){
            for(Host host:networkDatacenter.getHostList()){
                networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
            }
        }

        LOGGER.debug("N. Hosts: "+networkDatacenterList.get(0).getHostList().size());
        LOGGER.debug("N. Hosts: "+networkDatacenterList.get(0).getHostList().size());

        Broker2 broker2 = createBroker(cloudSim);
        int brokerId = (int) broker2.getId();

        List<Vm> vmList = createVMList( numberVms,0,new ArrayList<>());

        broker2.submitVmList(vmList);

        List<CustomCloudlet> mappersList = createCloudletList( numberMappers,CustomCloudlet.Type.MAPPER, 0,new ArrayList<>());
        List<CustomCloudlet> reducersList = createCloudletList( numberReducers, CustomCloudlet.Type.REDUCER, numberMappers,new ArrayList<>());


        /*addOnFinishLister added to all the Mappers. Once the Mapper finishes execution
        the corresponding Broker.SubmitReducer() method is called where a reducer is submitted
        on the same host (where 2 Mappers have finished execution)
        for execution */

        for (CustomCloudlet mapper : mappersList) {
            mapper.addOnFinishListener(new EventListener<CloudletVmEventInfo>() {
                @Override
                public void update(CloudletVmEventInfo info) {
                    CustomCloudlet mapper = (CustomCloudlet) info.getCloudlet();

                    broker2.submitReducer(mapper);

                }
            });
        }

        broker2.submitMapperList(mappersList);
        broker2.submitReducerList(reducersList);


        NetworkTopology networkTopology = new BriteNetworkTopology();
        cloudSim.setNetworkTopology(networkTopology);
        networkTopology.addLink(networkDatacenterList.get(0).getId(), broker2.getId(), 1000.0, 10);
        networkTopology.addLink(networkDatacenterList.get(1).getId(), broker2.getId(), 1000.0, 10);

        LOGGER.info("Simulation Started");

        cloudSim.start();

        List<CustomCloudlet> cloudletFinishedList = broker2.getCloudletFinishedList();

        LOGGER.info("Simulation Finished");
        LOGGER.info("+++++++++++++++ OUTPUT (CloudletScheduler: TimeShared Policy) ++++++++++++");
        LOGGER.info("Cloudlet_ID" + space + "STATUS" + space + "DataCenter_ID" + space + "VM_ID" + space + "Time" + space + "Start_Time" + space + "Finish_Time" + space + "Submission_Time" + space + "Total_Cost" + space + "Cloudlet_Type" + space + "HostID");

        cloudletFinishedList.forEach(Simulation2::displayResult);
        LOGGER.info("TotalCost: "+ String.format("%.2f", computeCost(cloudletFinishedList)));


        long end = System.currentTimeMillis();
        int timeTaken = (int) ((end - start) / 1000.0);
        LOGGER.info("The total time taken by the simulation: " + timeTaken + " seconds");
    }




    public static Broker2 createBroker(CloudSim simulation) throws Exception {
        return new Broker2(simulation,"Broker");
    }

    /*This method creates a list of VMs from the config file*/

    public static List<Vm> createVMList( int numVM, int  startId, List<Vm> vmList){

        if (numVM==0) {
            return vmList;
        }

        int mips = config.getInt(SIMULATION +".vm.mips");
        int imageSize = config.getInt(SIMULATION +".vm.imageSize");
        int pesNum = config.getInt(SIMULATION +".vm.pesNum");
        String vmm = config.getString(SIMULATION +".vm.vmm");
        int ram = config.getInt(SIMULATION +".vm.ram");
        int bw = config.getInt(SIMULATION +".vm.bw");

        Vm vm = new VmSimple( mips, pesNum,new CloudletSchedulerTimeShared());
        vm.setId(startId);
        vm.setRam(ram);
        vm.setBw(bw);
        vm.setSize(imageSize);
        vm.setDescription(vmm);
        LOGGER.info("VM"+startId+" (mips="+mips+",size="+imageSize+",ram="+ram+",bw="+bw+",numCPUS="+pesNum+") created");

        vmList.add(vm);

        createVMList( numVM-1, startId+1, vmList);
        return vmList;
    }

    /*This method creates a list of Data Centers recursively from the config file*/

    public static List<NetworkDatacenter> createDatacenterList(int numDc, int startId, List<NetworkDatacenter> dcList){

        if(numDc==0) {
            return dcList;
        }

        NetworkDatacenter datacenter = createDatacenter("datacenter"+startId, startId);

        dcList.add(datacenter);

        createDatacenterList(numDc-1, startId+1, dcList);
        return dcList;
    }

    public static NetworkDatacenter createDatacenter(String name, int startId){

        int numberHosts = config.getInt(SIMULATION +"."+name+".numHosts");
        List<Host> hostList = createHostList(numberHosts, startId, new ArrayList<>());
        String arch = config.getString(SIMULATION +"."+name+".arch");
        String os = config.getString(SIMULATION +"."+name+".os");
        String vmm = config.getString(SIMULATION +"."+name+".vmm");
        double cost = config.getDouble(SIMULATION +"."+name+".cost");
        double costPerMem = config.getDouble(SIMULATION +"."+name+".costPerMem") ;
        double costPerStorage = config.getDouble(SIMULATION +"."+name+".costPerStorage") ;
        double costPerBw = config.getDouble(SIMULATION +"."+name+".costPerBw") ;

        List<FileStorage> storageList = new ArrayList<FileStorage>();

        NetworkDatacenter networkDatacenter = new NetworkDatacenter(cloudSim,hostList, new VmAllocationPolicyBestFit());
        networkDatacenter.getCharacteristics().setArchitecture(arch)
                .setOs(os)
                .setVmm(vmm)
                .setCostPerMem(costPerMem)
                .setCostPerStorage(costPerStorage)
                .setCostPerBw(costPerBw)
                .setCostPerSecond(cost);
        networkDatacenter.getDatacenterStorage().setStorageList(storageList);


        return networkDatacenter;
    }


    public static void createPES(int numberPES, int startId, int mips, List<Pe> peList) {

        if (numberPES==0) {
            return;
        }
        peList.add(new PeSimple(startId, mips,new PeProvisionerSimple()));
        createPES(numberPES-1, startId+1, mips, peList);
    }

    /*This method creates a list of Hosts recursively from the config file*/

    public static List<Host> createHostList(int numList, int startId, List<Host> hostList) {
        int numCPU = config.getInt(SIMULATION +".host.numCpu");
        int mips = config.getInt(SIMULATION +".host.mips");

        if (numList==0) {
            return hostList;
        }

        List<Pe> peList = new ArrayList<Pe>();

        createPES(numCPU, 0, mips, peList);

        int ram = config.getInt(SIMULATION +".host.ram") ;
        int storage = config.getInt(SIMULATION +".host.storage");
        int bw = config.getInt(SIMULATION +".host.bw");
        double diskSpeed = config.getDouble(SIMULATION +".host.diskSpeed");

        CustomHost customHost = new CustomHost( ram,bw,storage,peList);
        customHost.setId(startId);
        customHost.setVmScheduler(new VmSchedulerTimeShared());

        customHost.setDiskSpeed(diskSpeed);

        hostList.add(customHost);

        createHostList(numList-1, startId+1, hostList);
        return hostList;
    }

    /*This method creates a list of Cloudlets recursively from the config file*/

    public static List<CustomCloudlet> createCloudletList( int numCloudlet, CustomCloudlet.Type cloudletType, int startId, List<CustomCloudlet> cloudletList){

        if (numCloudlet==0) {
            return cloudletList;
        }

        int pesNumber = config.getInt(SIMULATION +".cloudlet.pesNumber");
        int fileSize =  config.getInt(SIMULATION +".cloudlet.fileSize");
        int outputSize = config.getInt(SIMULATION +".cloudlet.outputSize");
        int cloudletLength = config.getInt(SIMULATION +".cloudlet.cloudletLength");
        UtilizationModelFull utilizationModel = new UtilizationModelFull();

        CustomCloudlet customCloudlet  = new CustomCloudlet( cloudletLength*2, pesNumber);
        LOGGER.info("CLOUDLET"+startId+" (OUTPUTSIZE="+outputSize+",LENGHT="+cloudletLength+",FILESIZE="+fileSize+") CREATED");


        customCloudlet.setType(cloudletType);
        customCloudlet.setId(startId);
        customCloudlet.setFileSize(fileSize);
        customCloudlet.setOutputSize(outputSize);
        customCloudlet.setUtilizationModel(utilizationModel);

        customCloudlet.setUtilizationModelBw(utilizationModel);
        customCloudlet.setUtilizationModelCpu(utilizationModel);
        customCloudlet.setUtilizationModelRam(utilizationModel);

        cloudletList.add(customCloudlet);

        createCloudletList( numCloudlet-1, cloudletType, startId+1, cloudletList);
        return cloudletList;
    }

    /*This method displays the result of the simulation in o formated way*/

    private static void displayResult(CustomCloudlet customCloudlet) {
        if (customCloudlet.getStatus() == Cloudlet.Status.SUCCESS) {
            String success="  SUCCESS";
            if (customCloudlet.getType()== CustomCloudlet.Type.MAPPER) {
                double costPerSec = customCloudlet.getTotalCost() *  customCloudlet.getActualCpuTime();
                LOGGER.info(space + customCloudlet.getId()
                        + space + space + success +
                        space + space +
                        customCloudlet.getLastTriedDatacenter().getId()
                        + space + space + space + customCloudlet.getVm().getId()
                        +    space+ decimalFormat.format(customCloudlet.getActualCpuTime())
                        + space+space+ decimalFormat.format(customCloudlet.getExecStartTime())
                        + space+space+ space + decimalFormat.format(customCloudlet.getFinishTime())
                        + space+space+ space + decimalFormat.format(customCloudlet.getExecStartTime())
                        + space + space + space + decimalFormat.format(costPerSec)
                        + space+ space + customCloudlet.getType ()   + space+ space+ space
                        + customCloudlet.getVm().getHost().getId());
            } else {
                double costPerSec = customCloudlet.getTotalCost() *  (customCloudlet.getActualCpuTime()+ customCloudlet.getProcessingDelay());
                LOGGER.info(space + customCloudlet.getId()
                        + space + space + success +
                        space + space +
                        customCloudlet.getLastTriedDatacenter().getId()
                        + space + space + space + customCloudlet.getVm().getId()
                        +    space+ decimalFormat.format(customCloudlet.getActualCpuTime())
                        + space+space+ decimalFormat.format(customCloudlet.getExecStartTime())
                        + space+space+ space + decimalFormat.format(customCloudlet.getFinishTime())
                        + space+space+ space + decimalFormat.format(customCloudlet.getExecStartTime())
                        + space + space + space + decimalFormat.format(costPerSec)
                        + space+ space + customCloudlet.getType () +"("+ customCloudlet.getAssociatedMappersList()+")"
                        + space+ space
                        + customCloudlet.getVm().getHost().getId());


            }


        } else {
            LOGGER.info(space + customCloudlet.getId() + space + space);
        }
    }

    /*This method calculates the total cost involved in the execution of all the cloudlets (Mappers and Reducers)*/

    private static double computeCost(List<CustomCloudlet> customCloudletList){

        if (customCloudletList.isEmpty()) {
            return 0.0;
        }

        double costPerSecond;
        CustomCloudlet customCloudlet = customCloudletList.get(0);

        if(customCloudlet.getType()== CustomCloudlet.Type.MAPPER){
            costPerSecond = customCloudlet.getCostPerSec() *  customCloudlet.getActualCpuTime();
        }else{
            costPerSecond = customCloudlet.getCostPerSec() *  (customCloudlet.getActualCpuTime()+customCloudlet.getProcessingDelay());
        }

        List<CustomCloudlet> subList = customCloudletList.subList(1, customCloudletList.size());

        return costPerSecond + computeCost(subList);

    }


}
