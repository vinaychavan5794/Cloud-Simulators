# Cloud-Simulators

#### Introduction

This project is about creating cloud simulators for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.

#### Instructions to run the simulations

Using IntellJ IDEA with the Scala plugin installed:
1.	Open IntelliJ IDEA, select “Check out from Version Control” and then “Git”.
2.	Enter the following URL and click Clone: git clone https://vinaychavan5794@bitbucket.org/cs441-fall2020/vinay_indrajit_chavan_hw1.git
3.	The SBT import screen will appear, proceed with the default options, and confirm with “OK”
4.	Go to src/main/java/com.vchava2/ and run the simulations. A run configuration is automatically created when you run any of the simulations.

Using Command Line Interface (CLI):

1. Execute command git clone https://vinaychavan5794@bitbucket.org/cs441-fall2020/vinay_indrajit_chavan_hw1.git
2. Run the code: sbt clean compile run
3. You will be asked to make a choice, please read the project structure section for more details


#### Project structure
In this section the project structure is described in detail.

#### Simulations
The following Simulations are provided:

Simulation1: Simulation showing Map/Reduce architecture (with Data Locality policy) and Time Sharing policy.
Simulation2: Simulation showing Map/Reduce architecture (without Data Locality policy) and Time Sharing policy.
Simulation3: Simulation showing Map/Reduce architecture (with Data Locality policy) and Space Sharing policy.
Simulation4: Simulation showing Map/Reduce architecture (without Data Locality policy) and Space Sharing policy.
Simulation5: Simulation showing execution of cloudlets (with different characteristics) on different datacenters, depending on the additional information associated with the cloudlets. (Task 5)

### Tests
The following test classes are provided:
BrokerTest
SimulationTest

The following methods are tested in Class SimulationTest:

testCreateDatacenter(): This test checks if the datacenter was successfully created.
testCreatedVM(): This test checks if the number of VMs created equal the number of VMs specified in the config file.
testCreatedHost(): This test checks if the number of hosts created in the datacenter equal the number of hosts mentioned in the config file.

The following methods are tested in Class BrokerTest :

testCloudletMapperType(): This test checks that all Mappers are successfully submitted to the broker and also checks if the CloudLet type is Mapper when retrieved.
testCloudletMapper(): This test checks if the number of Mappers created equal the number of Mappers mentioned in the config file.
testSimulation(): This test mocks a simulation of broker1 policy where 2 mappers are created and submitted first. 

•	Case 1: Once both the mappers finish their execution, broker submits the reducer to the same host where both the mappers executed. In this case no data transfer will be involved. (Data Locality Policy)
•	Case 2: In case mappers executed on different hosts, (diskspeed*fileSize) cost is added to the reducer execution cost as there is data transfer involved between different hosts and the reducer will be allocated to one of the two host.

#### Configuration
simulation1.conf and simulation2.conf have configuration options for the following entities:

•	VM
•	Cloudlet
•	Datacenter
•	Host

#### Implemented policy

In this section the implemented policy is described in detail:
Custom disk speed parameter is added to Host entity. This helps us to consider the delay in data transfer when starting a map/reduce job.
In case if the reducer is submitted to a Host where only 1 mapper was executed, this data transfer delay will be modelled through a disk speed that is added to the cloudlet start-up time.

#### Broker1 Policy
This policy checks if 2 mappers have executed on the same host. If so, 1 reducer is allocated to the same host so that there is no data transfer. If all the mappers have finished executing, then the remaining reducers are allocated to the hosts (in a round robin fashion) where remaining mappers were executed. In case mappers executed on different hosts (diskspeed*fileSize) cost is added to the reducer execution cost as there is data transfer involved between different hosts. This policy tries to minimize the execution time of the reducer by minimizing the excessive data transfer between the host.

Consider a map/reduce job running on the cloud, each data chunk is sent to different Mappers. Data needs to be transferred between different hosts, which causes a certain delay. Also, the transfer speed is limited by the disk speed.

Once Mappers complete their execution, their output can be further processed by Reducers. However, there are two scenarios with respect to Reducer scheduling:

•	Scenario 1: No data needs to be transferred if the reducer is started on the same host as the mappers (Since the data is already available on the local disk).
•	Scenario 2: Data needs to be transferred between hosts if the reducer is started on a different host than at least one of the mappers: In this case we need to consider disk speed while transfering data between hosts.

Hence, the proposed Broker1 policy will try to allocate a reducer to a host where 2 mappers have already finished execution to minimize the execution time and thus no data transfer will occur(delay). Please refer classes Broker1.java and CustomCloudlet.java for more details.
#### Limitations with Broker1 Policy:
The policy is not effective if most of the mappers run on different hosts. In that case, data transfer will be unavoidable which will increase the execution time of the reducer. 
#### Broker2 Policy
This Policy maintains a list of mappers that were executed on a particular host. Once all the mappers have finished their execution, it starts allocating reducers to the host. If there are more than 1 mappers that were executed on the same host, then 1 reducer is allocated to that host and 2 mappers are removed from the corresponding host's list of mappers.  In this case there will be no data transfer cost involved. If only a single mapper executed on a host, then (diskspeed *filesize) cost will be added to the reducer execution cost (data transfer involved between different hosts), reducer will be allocated to that host and 1 mapper is removed from the corresponding host's list of mappers. In case there were no mappers executed on a host, then resource wastage cost will be added to the reducer execution cost and the reducer will be allocated to that host. Please refer classes Broker2.java and CustomCloudlet.java for more details.

#### Limitations with Broker2 Policy
The policy is not effective if most of the mappers run on different hosts. In that case, data transfer will be unavoidable which will increase the execution time of the reducer. Also, if most of the reducers execute on hosts where no mappers executed, then a lot of resource wastage cost will be added to the reducer execution cost.
 Broker1 vs Broker2 Policy: Broker1 policy performs better than Broker2 as the reducers in case of Broker1 do not have to wait until all the mappers have finished their execution. Also, in case of Broker1 policy, there is no resource wastage cost involved.

#### Analysis of results:

In this section the results of the different simulations are discussed and reported
Four Simulations are described as follows:

1.)	Simulation 1: Data Locality Policy (Map/ Reduce architecture) i.e. Broker1 policy with Time Shared Cloudlet policy.
2.)	Simulation 2: Broker2 Policy (Map/ Reduce architecture) with Time Shared Cloudlet policy.
3.)	Simulation 3: Data Locality Policy (Map/ Reduce architecture) i.e. Broker1 policy with Space Shared Cloudlet policy.
4.)	Simulation 4: Broker2 Policy (Map/ Reduce architecture) with Space Shared Cloudlet policy.

#### Metric: cost

Monetary cost is considered to compare simulation performance which will further decide which cloud architecture is better.
The cost is computed as the cost per second multiplied by the actual run time of the cloudlet plus the time taken to transfer data between the cloudlets: COST_PER_SECOND * (CPU_TIME + DELAY_DATA_TRANSFER)

The total cost of the four simulations is as follows:

Simulation1: 713.38 cost units
Simulation2: 1118.38 cost units
Simulation3: 469.80 cost units
Simulation4: 874.80 cost units

From the above total cost figures, we see that Simulation3 yields the lowest cost across all the simulations. In both Broker1 and Broker2 policy a SpaceShared Cloudlet policy ended up being more efficient than Timeshared Cloudlet Policy.

#### TimeShared vs SpaceShared

The costs in the TimeShared environment are higher than in the SpaceShared with respect to individual cloudlet cost; this happens because whenever there are not  sufficient VMs to run all the cloudlets independently, the TimeShared policy allocates cloudlets on the same VM and executes them parallely, therefore  resulting in higher cost. In other words,in case of Timeshared policy two cloudlets use the same CPU alternatively (Share CPU).

#### Broker1 policy vs Broker2 policy

The cost of simulation in case of Broker1 policy is less as compared to the Broker2 policy. The reason being , in our simulation we assume that whenever the mapper(s) and the respective reducer run on the same host, no data transfer is required between the two and as a result there is no delay .While on the other hand, when mapper(s) and reducer run on different hosts, data needs to be transferred between the two hosts, which takes some time due to the limited disk speed. Also, in case of Broker1 policy, there is no resource wastage cost involved. 



#### Task 5:

Broker3:  It allocates cloudlets to different datacenters depending on the additional information provided with each cloudlet. 

•	Cloudlet Type1: Complete Cloudlet configuration information is provided such as number of PE, File Size, Output Size, MIPS, Utilization model for ram,cpu and bw along with additional information including choice of OS and Cloudlet Scheduler Policy. The Broker allocates the cloudlet to datacenter DC0 (Mix of PaaS and SaaS). 

•	Cloudlet Type2: Complete Cloudlet configuration information is provided such as number of PE, File Size, Output Size, MIPS, Utilization model for ram,cpu and bw along with additional information such as Host’s VM scheduler policy and required ram for VM.  The cloudlet is allocated to datacenter DC1 (Mix of SaaS and IaaS) by the Broker. In this case the Broker also decides the VM configuration (such as PE, mips) required for the Cloudlet execution.

•	Cloudlet Type3:  Only MIPS information is provided along with Utilization model for ram,cpu and bw. The cloudlet is allocated to datacenter DC2. (Mix of FaaS and SaaS). Broker decides the VM configuration (on which the cloudlet will execute) and how many PE the cloudlet would require.

Simulation5: It creates number of Cloudlet Type 1 ,2 and 3 (User Tasks) and Datacenters DC0, D1 and DC2 as specified in the config file. Once all the cloudlets are created, it submits them to the Broker. The Broker then allocates each cloudlet to a particular datacenter based on the additional information provided with each cloudlet. 
 

#### Analysis of results:

#### Metric: cost
Monetary cost is considered to compare simulation performance which will further decide which combination of cloud architecture (IaaS, PaaS, SaaS and FaaS) is better.
The cost is computed as the cost per second multiplied by the actual run time of the cloudlet plus the cost of using additional services  multiplied by the actual run time of the cloudlet: (COST_PER_SECOND * CPU_TIME ) * (COST_OF_DC_USAGE * CPU_TIME )

The total cost for executing each Cloutlet:
•	Cloudlet Type 1: 26.99 cost units
•	Cloudlet Type 2: 27.83 cost units
•	Cloudlet Type 3: 1.03 cost units

From the above result, it is evident that executing Cloudlet Type 2 is more expensive as compared to other Cloudlet Types. Reason being, Cloudlet Type 2 demands services such as user choice of Host’s VM scheduler policy,VM configuration (Ram to be 1 GB),Cloudlet configuration. Cloudlet Type 1 requests for a particular CloudletScheduler policy in addition to  Cloudlet configuration and choice of Operating System.  Cloudlet Type 3 is least expensive in terms of execution cost, because it demands least services which is just CloudLet Configuration. 

#### Datacenter:

•	DC0: It is a mix of PaaS and SaaS. In this datacenter, users can change the Cloudlet Scheduler policy, set the type of OS, set Cloudlet configuration. Cloudlets and VMs are submitted to the datacenter through Broker.

•	DC1: It is a mix of SaaS and IaaS. In this datacenter, users can change the VM scheduler policy of the host, set Cloudlet configuration,set VM configuration. Cloudlets and VMs are submitted to the datacenter through Broker.

•	DC2: It is a mix of FaaS and SaaS. In this datacenter, users can change the Cloudlet configuration. Cloudlets and VMs are submitted to the datacenter through Broker.



