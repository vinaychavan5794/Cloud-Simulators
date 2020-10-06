
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.vchava2.Broker1;
import com.vchava2.CustomCloudlet;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.vchava2.Simulation1.*;
import static org.junit.Assert.assertEquals;

public class BrokerTest {

    String SIMULATION ="simulation2";
    Config config =  ConfigFactory.load(SIMULATION +".conf");
    CloudSim cloudSim;
    Broker1 broker1;
    int brokerId;
    int MAPPERS_COUNT;
    int numHosts;
    NetworkDatacenter networkDatacenter;

    @Before
    public void init() throws Exception {
        cloudSim = new CloudSim();
        MAPPERS_COUNT = config.getInt(SIMULATION +".numberMappers");
        String dc="datacenter0";

        numHosts = config.getInt(SIMULATION +"."+dc+".numHosts");

        String arch = config.getString(SIMULATION +"."+dc+".arch");
        String os = config.getString(SIMULATION +"."+dc+".os");
        String vmm = config.getString(SIMULATION +"."+dc+".vmm");
        double costPerStorage = config.getDouble(SIMULATION +"."+dc+".costPerStorage") ;
        double costPerBw = config.getDouble(SIMULATION +"."+dc+".costPerBw") ;
        double cost = config.getDouble(SIMULATION +"."+dc+".cost");
        double costPerMem = config.getDouble(SIMULATION +"."+dc+".costPerMem") ;

        List<FileStorage> storageList = new ArrayList<FileStorage>();
        cloudSim = new CloudSim();
        networkDatacenter = new NetworkDatacenter(cloudSim,createHostList(numHosts, 0,new ArrayList<>()), new VmAllocationPolicySimple());
        networkDatacenter.getCharacteristics().setArchitecture(arch)
                .setOs(os)
                .setVmm(vmm)
                .setCostPerMem(costPerMem)
                .setCostPerStorage(costPerStorage)
                .setCostPerBw(costPerBw)
                .setCostPerSecond(cost);
        networkDatacenter.getDatacenterStorage().setStorageList(storageList);
        networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
        networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
        networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
        networkDatacenter.addSwitch(new EdgeSwitch(cloudSim,networkDatacenter));
        broker1 = createBroker(cloudSim);
        brokerId = (int) broker1.getId();

    }

    /*This test checks if the Type of the created Cloudlets if Mapper
     * */

    @Test
    public void testCloudletMapperType() throws Exception {

        List<CustomCloudlet> mappersList = createCloudletList( MAPPERS_COUNT,CustomCloudlet.Type.MAPPER, 0,new ArrayList<>() );
        broker1.submitMapperList(mappersList);

        List<Cloudlet> submittedCloudletsList = broker1.getCloudletCreatedList();
        for(Cloudlet cloudlet:submittedCloudletsList){

            assertEquals(((CustomCloudlet) cloudlet).getType(), CustomCloudlet.Type.MAPPER);
        }

    }


    /*This test checks if the number of Mappers that were created
     * equals the number of Mappers specified in the config file*/

    @Test
    public void testCloudletMapper() throws Exception {



        List<CustomCloudlet> mappersList = createCloudletList( MAPPERS_COUNT,CustomCloudlet.Type.MAPPER ,
                0,new ArrayList<>() );
        broker1.submitMapperList(mappersList);

        assertEquals(broker1.getCloudletSubmittedList().size(),MAPPERS_COUNT);


    }


    /*This test simulates Broker1 policy by submitting 2
    mappers and 1 reducer to the broker. It further checks if the last executed
    cloudlet was reducer*/

    @Test
    public void testSimulation(){

        List<Vm> vmlist = createVMList( 3,0,new ArrayList<>());
        broker1.submitVmList(vmlist);

        List<CustomCloudlet> mappersList = createCloudletList( 2,CustomCloudlet.Type.MAPPER, 0,new ArrayList<>() );
        List<CustomCloudlet> reducersList  = createCloudletList( 1,CustomCloudlet.Type.REDUCER, 2,new ArrayList<>() );


        for (CustomCloudlet mapper : mappersList) {
            mapper.addOnFinishListener(new EventListener<CloudletVmEventInfo>() {
                @Override
                public void update(CloudletVmEventInfo info) {
                    CustomCloudlet mapper = (CustomCloudlet) info.getCloudlet();

                    broker1.submitReducer(mapper);

                }
            });
        }


        broker1.submitMapperList(mappersList);
        broker1.submitReducerList(reducersList);

        cloudSim.start();


        List<CustomCloudlet> newList = broker1.getCloudletFinishedList();

        CustomCloudlet lastCloudlet = newList.get(newList.size()-1);

        assert(lastCloudlet.getType()== CustomCloudlet.Type.REDUCER);
    }







}
