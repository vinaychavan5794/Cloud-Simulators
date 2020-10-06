
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.vchava2.Broker1;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static com.vchava2.Simulation1.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class SimulationTest {

    String SIMULATION ="simulation2";
    Config config =  ConfigFactory.load(SIMULATION+".conf");
    CloudSim cloudSim;
    NetworkDatacenter networkDatacenter;
    int numHosts;

    @Before
    public void init() {
        String dc="datacenter0";
        numHosts = config.getInt(SIMULATION +"."+dc+".numHosts");
        String arch = config.getString(SIMULATION +"."+dc+".arch");
        String os = config.getString(SIMULATION +"."+dc+".os");
        double costPerMem = config.getDouble(SIMULATION +"."+dc+".costPerMem") ;
        double costPerStorage = config.getDouble(SIMULATION +"."+dc+".costPerStorage") ;
        double costPerBw = config.getDouble(SIMULATION +"."+dc+".costPerBw") ;
        String vmm = config.getString(SIMULATION +"."+dc+".vmm");
        double cost = config.getDouble(SIMULATION +"."+dc+".cost");

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

    }


    /*This test checks if the datacenter0 was successfully created*/

    @Test
    public void testCreateDatacenter() throws Exception {

      assertNotEquals(networkDatacenter,null);

    }

    /*This test checks if the number of VMS that were created
    * equals the number of VMS specified in the config file*/

    @Test
    public void testCreatedVM() throws Exception {

        Broker1 broker1 = createBroker(cloudSim);
        int numVM= config.getInt(SIMULATION +".numberVms");
        List<Vm> vmlist = createVMList( numVM,0,new ArrayList<>());
        assertEquals(vmlist.size(),numVM);

    }

    /*This test checks if the number of HOSTS (in DC0) that were created
     * equals the number of HOSTS specified in the config file*/

    @Test
    public void testCreatedHost() throws Exception {

        assertEquals(networkDatacenter.getHostList().size(),numHosts);

    }








}
