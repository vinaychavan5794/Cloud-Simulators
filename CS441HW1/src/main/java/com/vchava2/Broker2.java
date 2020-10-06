package com.vchava2;


import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Broker2 extends DatacenterBrokerSimple {

    private static Logger LOG = LoggerFactory.getLogger(Simulation1.class);
    private ArrayList<CustomCloudlet> reducerList = null;
    private ArrayList<CustomCloudlet> mapperList = null;
    private Map<Host,List<CustomCloudlet>> mappersFinishedList  =null;
    private ArrayList<Integer> inputMapperList=null;
    private List<Vm>vmList=null;
    private int countFinishedMappers =0;
    private int startID=0;

    public Broker2(CloudSim simulation, String name) throws Exception {
        super(simulation,name);
        reducerList = new ArrayList<>();
        mapperList =new ArrayList<>();
        mappersFinishedList=new HashMap<>();
        inputMapperList=new ArrayList<>();
        vmList=new ArrayList<>();
    }

    public void submitReducerList(List<? extends CustomCloudlet> list) {
        reducerList.addAll(list);
    }

    public void submitMapperList(List<? extends CustomCloudlet> list) {
        super.submitCloudletList(list);
        mapperList.addAll(list);
    }

    /*This method maintains a list of mappers that were executed on a particular host.
    Once all the mappers have finished their execution,it starts allocating reducers to the host.
    If there are more than 1 mappers that executed on the same host,then 1 reducer
    is allocated to that host and 2 mappers are removed from the corresponding host's list of mappers.
    In this case there will be no data transfer cost involved.
    If only a single mapper executed on a host,then (diskspeed*file size) cost will be added to the
    reducer execution cost (data transfer involved between different hosts),
    reducer will be allocated to that host and 1 mapper is removed from the corresponding
    host's list of mappers. In case there were no mappers executed on a host, then
    resource wastage cost will be added to the reducer execution cost and the reducer will be allocated
    to that host.
     * */

    public void submitReducer(CustomCloudlet current){

        countFinishedMappers++;
        if(mappersFinishedList.containsKey(current.getVm().getHost())){
            mappersFinishedList.get(current.getVm().getHost()).add(current);
        }else{
            mappersFinishedList.put(current.getVm().getHost(),new ArrayList<>());
            mappersFinishedList.get(current.getVm().getHost()).add(current);
        }


        if(countFinishedMappers== mapperList.size()){

            while (reducerList.size()>0){

                for (Map.Entry<Host,List<CustomCloudlet>> entry : mappersFinishedList.entrySet()) {
                    if(reducerList.size()==0){
                        break;
                    }
                    if(startID==49){
                        startID=0;
                    }else{
                        startID++;
                    }

                    inputMapperList.clear();
                    CustomCloudlet customCloudlet = reducerList.get(0);

                    //checks if there were more than 1 mapper executed on the same host.
                    if(entry.getValue().size()>1){
                        inputMapperList.add((int)entry.getValue().get(0).getId());
                        inputMapperList.add((int)entry.getValue().get(1).getId());
                        List<Integer> temp = new ArrayList<>(inputMapperList);
                        customCloudlet.setHost(entry.getKey());
                        vmList=Simulation1.createVMList(1,startID,new ArrayList<>());
                        vmList.get(0).setHost(entry.getKey());
                        customCloudlet.setAssociatedMappersList(temp);
                        customCloudlet.setProcessingDelay(0.0);
                        reducerList.remove(0);
                        entry.getValue().remove(0);
                        entry.getValue().remove(0);
                        submitVmList(vmList);
                        submitCloudlet(customCloudlet);
                    }

                    //checks if only a single mapper executed on a host
                    else if (entry.getValue().size()==1){
                        inputMapperList.add((int)entry.getValue().get(0).getId());
                        customCloudlet.setProcessingDelay(((CustomCloudlet)entry.getValue().get(0)).getFileSize()*
                                ((CustomHost)entry.getKey()).getDiskSpeed());
                        List<Integer> temp = new ArrayList<>(inputMapperList);
                        customCloudlet.setHost(entry.getKey());
                        vmList=Simulation1.createVMList(1,startID,new ArrayList<>());
                        vmList.get(0).setHost(entry.getKey());
                        customCloudlet.setAssociatedMappersList(temp);
                        reducerList.remove(0);
                        entry.getValue().remove(0);
                        submitVmList(vmList);
                        submitCloudlet(customCloudlet);
                    }

                    // Case where no mapper executed on a host.
                    else{
                        customCloudlet.setHost(entry.getKey());
                        vmList=Simulation1.createVMList(1,startID,new ArrayList<>());
                        vmList.get(0).setHost(entry.getKey());
                        customCloudlet.setAssociatedMappersList(inputMapperList);
                        customCloudlet.setProcessingDelay(3.0); // resource wastage cost added
                        reducerList.remove(0);
                        submitVmList(vmList);
                        submitCloudlet(customCloudlet);
                    }


                }
            }

        }

        LOG.info("Number reducers remainging: "+ reducerList.size());


    }

}
