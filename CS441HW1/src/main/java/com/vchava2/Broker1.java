package com.vchava2;


import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Broker1 extends DatacenterBrokerSimple {

    static Logger LOG = LoggerFactory.getLogger(Simulation1.class);
    private ArrayList<CustomCloudlet> reducerList = null;
    private ArrayList<CustomCloudlet> mapperList = null;
    private Map<Host,List<Cloudlet>> hostToMapper=null;
    private ArrayList<Integer> inputMapperList=null;
    private int countFinishedMappers =0;
    private List<Vm>vmList=null;
    private int startID=0;


    public Broker1(CloudSim simulation, String name) throws Exception {
        super(simulation,name);
        reducerList = new ArrayList<>();
        mapperList =new ArrayList<>();
        hostToMapper=new HashMap<>();
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

    /* This method checks if 2 mappers have executed on the same host. If so, 1 reducer is
    *  allocated to the same host so that there is no data transfer. If all the mappers have finished
    *  executing, then the remaining reducers are allocated to the hosts (in a round robin fashion)
    *  where remaining mappers were executed.In case mappers executed on different hosts
    *  (diskspeed*fileSize) cost is added to the reducer execution cost
    *  as there is data transfer involved between different hosts.
    * */


    public void submitReducer(CustomCloudlet current){

           countFinishedMappers++;

           if (hostToMapper.containsKey(current.getVm().getHost())) {
               hostToMapper.get(current.getVm().getHost()).add(current);

           } else {
               hostToMapper.put(current.getVm().getHost(), new ArrayList<>());
               hostToMapper.get(current.getVm().getHost()).add(current);
           }

           for (Map.Entry<Host, List<Cloudlet>> entry : hostToMapper.entrySet()) {
               inputMapperList.clear();

               /*Checks if 2 mappers were executed on the same Host. If so, 1 reducer is allocated
               * to the same host. In this case there will be no data transfer involved
               * (Data locality)*/

               if (entry.getValue().size() >= 2) {

                   for (Cloudlet myCloudlets : entry.getValue()) {
                       inputMapperList.add((int) myCloudlets.getId());
                   }
                   if(startID==49){
                       startID=0;
                   }else{
                       startID++;
                   }

                   List<Integer> temp = new ArrayList<>(inputMapperList);

                   CustomCloudlet customCloudlet = reducerList.get(0);

                   customCloudlet.setHost(entry.getValue().get(0).getVm().getHost());
                   vmList=Simulation1.createVMList(1,startID,new ArrayList<>());
                   vmList.get(0).setHost(entry.getValue().get(0).getVm().getHost());

                   customCloudlet.setAssociatedMappersList(temp);
                   customCloudlet.setProcessingDelay( 0.0);

                   submitVmList(vmList);
                   submitCloudlet(customCloudlet);
                   reducerList.remove(0);
                   hostToMapper.get(entry.getKey()).clear();


               }

           }

           /*Once all the mappers have finished executing,reducers are allocated to
           hosts where remaining mappers were executed.In case mappers execute on different hosts,
           data transfer will happen and additional data transfer cost will be added
           to the reducer execution cost.
           * */

           if(countFinishedMappers == mapperList.size()){

               hostToMapper.entrySet().removeIf(entry -> entry.getValue().size()==0);

               if(hostToMapper.size()>0){
                   while (reducerList.size()>0){
                       for(Map.Entry<Host,List<Cloudlet>> entry: hostToMapper.entrySet()){
                           inputMapperList.clear();

                                if(reducerList.size()==0){
                                    break;
                                }else{
                                    if(startID==49){
                                        startID=0;
                                    }else{
                                        startID++;
                                    }
                                    CustomCloudlet customCloudlet = reducerList.get(0);
                                    inputMapperList.add((int)entry.getValue().get(0).getId());
                                    List<Integer> temp1 = new ArrayList<>(inputMapperList);

                                    customCloudlet.setHost(entry.getValue().get(0).getVm().getHost());
                                    vmList=Simulation1.createVMList(1,startID,new ArrayList<>());

                                    vmList.get(0).setHost(entry.getValue().get(0).getVm().getHost());
                                    customCloudlet.setProcessingDelay(((CustomCloudlet)entry.getValue().get(0)).getFileSize()*
                                            ((CustomHost)entry.getKey()).getDiskSpeed());
                                    customCloudlet.setAssociatedMappersList(temp1);
                                    submitVmList(vmList);
                                    submitCloudlet(customCloudlet);
                                    reducerList.remove(0);
                                }

                       }
                   }

               }

           }

       LOG.debug("Number reducers remainging: "+ reducerList.size());

    }

}
