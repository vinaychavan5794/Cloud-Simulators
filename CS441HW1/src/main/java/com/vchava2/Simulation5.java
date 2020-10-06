package com.vchava2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Simulation5  {

    public static String SIMULATION = "simulation1";
    public static Config config =  ConfigFactory.load(SIMULATION +".conf");
    public static Logger LOGGER = LoggerFactory.getLogger(Simulation5.class);
    public static CloudSim cloudSim;
    public static String space = "    ";
    public static DecimalFormat decimalFormat = new DecimalFormat("###.##");
    public static double totalCost=0.0;


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Starting Simulation ..");

        int numberDcs = config.getInt(SIMULATION +".numberDcs");
        int numberType1Cloudlet = config.getInt(SIMULATION +".num_cloudlets_type1");
        int numberType2Cloudlet = config.getInt(SIMULATION +".num_cloudlets_type2");
        int numberType3Cloudlet = config.getInt(SIMULATION +".num_cloudlets_type3");

        cloudSim = new CloudSim();



        List<NetworkDatacenter> networkDatacenterList =
                createDatacenterList(numberDcs,0,new ArrayList<>());
        List<CustomCloudlet2> customCloudlet2List=new ArrayList<>();


        //Edge Switches added to the DC depending on the number of host in DC
        for(NetworkDatacenter networkDatacenter:networkDatacenterList){
            for(Host host:networkDatacenter.getHostList()){
                networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
            }
        }

        LOGGER.debug("Number of Hosts: in DC 0"+networkDatacenterList.get(0).getHostList().size());
        LOGGER.debug("Number of Hosts: in DC 1: "+networkDatacenterList.get(1).getHostList().size());
        LOGGER.debug("Number of Hosts: in DC 2: "+networkDatacenterList.get(2).getHostList().size());

        Broker3 broker3 = createBroker(cloudSim);
        createCloudletList(numberType1Cloudlet,0,"Type1",customCloudlet2List);
        createCloudletList(numberType2Cloudlet,numberType1Cloudlet,"Type2",customCloudlet2List);
        createCloudletList(numberType3Cloudlet,numberType2Cloudlet+1,"Type3",customCloudlet2List);

        broker3.submitTaskList(customCloudlet2List,networkDatacenterList);

        //Network topology created

        NetworkTopology networkTopology = new BriteNetworkTopology();
        cloudSim.setNetworkTopology(networkTopology);
        networkTopology.addLink(networkDatacenterList.get(0).getId(), broker3.getId(), 1000.0, 10);
        networkTopology.addLink(networkDatacenterList.get(1).getId(), broker3.getId(), 1000.0, 10);
        networkTopology.addLink(networkDatacenterList.get(2).getId(), broker3.getId(), 1000.0, 10);



        LOGGER.info("Simulation Started");

        cloudSim.start();

        List<CustomCloudlet2> cloudletFinishedList = broker3.getCloudletFinishedList();

        LOGGER.info("Simulation Finished");
        LOGGER.info("Cloudlet_ID" + space + "STATUS" + space + "DataCenter_ID" + space + "VM_ID" + space + "Time" + space + "Start_Time" + space + "Finish_Time" + space + "Submission_Time" + space + "Total_Cost" );

        cloudletFinishedList.forEach(Simulation5::displayResult);
        LOGGER.info("Total Cost for executing all the submitted cloudlets: "+
                String.format("%.2f", totalCost));


        long endTime = System.currentTimeMillis();
        int timeTaken = (int) ((endTime - startTime) / 1000.0);
        LOGGER.info("The total time taken by the simulation: " + timeTaken + " seconds");
    }


    public static Broker3 createBroker(CloudSim simulation) throws Exception {
        return new Broker3(simulation,"Broker");
    }

    /*This method creates a list of Data Centers recursively from the config file*/

   public static List<NetworkDatacenter> createDatacenterList(int numberOfDc, int startId, List<NetworkDatacenter> networkDatacenterList){

        List<NetworkDatacenter> newtworkDatacenterList =  new ArrayList<NetworkDatacenter>() ;

       if(numberOfDc==0) {
           return newtworkDatacenterList;
       }

       NetworkDatacenter datacenter = createDatacenter("datacenter"+startId, startId);

       networkDatacenterList.add(datacenter);
       createDatacenterList(numberOfDc-1, startId+1, networkDatacenterList);

       return networkDatacenterList;

    }

    /*This method creates a list of Hosts recursively from the config file*/

    public static List<Host> createHostList(int numberHost, int start, List<Host> hostList) {
        int numPE = config.getInt(SIMULATION +".host.numCpu");
        int mips = config.getInt(SIMULATION +".host.mips");

        if (numberHost==0) {
            return hostList;
        }

        List<Pe> peList = createPEList(numPE, 0, mips, new ArrayList<>());


        int ram = config.getInt(SIMULATION +".host.ram") ;
        int storage = config.getInt(SIMULATION +".host.storage");
        int bw = config.getInt(SIMULATION +".host.bw");

        CustomHost customHost = new CustomHost( ram,bw,storage,peList);
        customHost.setId(start);
        customHost.setVmScheduler(new VmSchedulerTimeShared());


        hostList.add(customHost);

        createHostList(numberHost-1, start+1, hostList);
        return hostList;
    }

    public static NetworkDatacenter createDatacenter(String name, int startId){



        int numHosts = config.getInt(SIMULATION +"."+name+".numHosts");
        List<Host> hostList = createHostList(numHosts, 0, new ArrayList<>());


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



    public static List<Pe> createPEList(int num, int start, int mips, List<Pe> peList) {

        if (num==0) {
            return peList;
        }

        peList.add(new PeSimple(start, mips,new PeProvisionerSimple()));
        createPEList(num-1, start+1, mips, peList);
        return peList;
    }

    public static CustomCloudlet2 createCloudlet( String type, int startId){

        int pesNumber = config.getInt(SIMULATION +".cloudlet.pesNumber");
        int fileSize =  config.getInt(SIMULATION +".cloudlet.fileSize");
        int outputSize = config.getInt(SIMULATION +".cloudlet.outputSize");
        int cloudletLength = config.getInt(SIMULATION +".cloudlet.cloudletLength");
        int numPE = config.getInt(SIMULATION +".host.numCpu");
        UtilizationModelFull utilizationModel = new UtilizationModelFull();
        CustomCloudlet2 cloudlet=null;

        //cloudlet that will be sent to DC0 (PaaS + SaaS)

        if(type.equals("Type1")){
            cloudlet  = new CustomCloudlet2( cloudletLength*2, pesNumber);
            cloudlet.setOs("Windows");
            cloudlet.setCloudletSchedulerAbstract(new CloudletSchedulerSpaceShared());
        }
        //cloudlet that will be sent to DC2 (FaaS+SaaS)
        else if(type.equals("Type3")){
            cloudlet  = new CustomCloudlet2( cloudletLength*2, numPE);
            cloudlet.setId(startId);
            cloudlet.setUtilizationModel(utilizationModel);
            cloudlet.setUtilizationModelBw(utilizationModel);
            cloudlet.setUtilizationModelCpu(utilizationModel);
            cloudlet.setUtilizationModelRam(utilizationModel);
            return cloudlet;
        }
        //cloudlet that will be sent to DC1 (SaaS+ IaaS)
        else{
            cloudlet  = new CustomCloudlet2( cloudletLength*2, pesNumber);
            cloudlet.setVmSchedulerAbstract(new VmSchedulerSpaceShared());
            cloudlet.setRam(1024);

        }
        cloudlet.setId(startId);
        cloudlet.setFileSize(fileSize);
        cloudlet.setOutputSize(outputSize);
        cloudlet.setUtilizationModel(utilizationModel);
        cloudlet.setUtilizationModelBw(utilizationModel);
        cloudlet.setUtilizationModelCpu(utilizationModel);
        cloudlet.setUtilizationModelRam(utilizationModel);

        LOGGER.info("CLOUDLET"+startId+" (OUTPUTSIZE="+outputSize+",LENGHT="+cloudletLength+",FILESIZE="+fileSize+") CREATED");



        return cloudlet;
    }

    /*This method creates a list of Cloudlets recursively from the config file*/

    public static void createCloudletList(int num, int startId,String type , List<CustomCloudlet2> customCloudlet2List) {

        if (num==0) {
            return;
        }

        customCloudlet2List.add(createCloudlet(type,startId));
        createCloudletList(num-1, startId+1, type, customCloudlet2List);
    }



    /*This method displays the result of the simulation in o formated way*/

    private static void displayResult(CustomCloudlet2 customCloudlet) {
        if (customCloudlet.getStatus() == Cloudlet.Status.SUCCESS) {
            String success="  SUCCESS";

            double costPerSec;
            if(!customCloudlet.getOs().equals("")){
                costPerSec= customCloudlet.getTotalCost() *  customCloudlet.getActualCpuTime();
                costPerSec=costPerSec+(customCloudlet.getNetworkDatacenter().getCharacteristics().getCostPerSecond()*  customCloudlet.getActualCpuTime());
            }else if(customCloudlet.getFileSize()>1){
                costPerSec= customCloudlet.getTotalCost() *  customCloudlet.getActualCpuTime();
                costPerSec=costPerSec+(customCloudlet.getNetworkDatacenter().getCharacteristics().getCostPerSecond()*  customCloudlet.getActualCpuTime());
            }else{
                costPerSec= customCloudlet.getTotalCost() *  customCloudlet.getActualCpuTime();
                costPerSec=costPerSec+(customCloudlet.getNetworkDatacenter().getCharacteristics().getCostPerSecond()*  customCloudlet.getActualCpuTime());
            }
            totalCost+=costPerSec;

            LOGGER.info(space +
                    customCloudlet.getId() +
                    space + space +
                    success
                    + space
                    + space
                    + customCloudlet.getNetworkDatacenter().getId()+
                    space + space + space + customCloudlet.getVm().getId()
                    + space + space + decimalFormat.format(customCloudlet.getActualCpuTime())
                    + space + space + decimalFormat.format(customCloudlet.getExecStartTime())
                    + space + space  + decimalFormat.format(customCloudlet.getFinishTime())
                    + space + space+ space  + decimalFormat.format(customCloudlet.getExecStartTime())
                    + space  + space+ space + decimalFormat.format(costPerSec)
                    );

        } else {
            LOGGER.info(space + customCloudlet.getId() + space + space);
        }
    }




}
