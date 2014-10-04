/*
very important!
change the sendNow function in broker class to the following:

send(getVmsToDatacentersMap().get(vm.getId()),cloudlet.getExecStartTime(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);

*/


package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.provisioners.*;

public class Problem4 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletlist;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Log.printLine("Problem 4:");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 1024; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			//setup 400 vms
			while(vmid<50){
			// create VM
			Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()); //This implements Time sharing
			// add the VMs to the vmList
			vmlist.add(vm);
			vmid++;
			}
			

			// submit vms list to the broker
			broker.submitVmList(vmlist);

			// Cloudlet properties
			final long length = 1440000;
			final long fileSize = 300; // size in kB
			final long outputSize = 300;
			final UtilizationModel utilizationModel = new UtilizationModelFull();
			
			// Fifth step: Create one Cloudlet
			cloudletlist = new ArrayList<Cloudlet>();

		
			int j=0;
			for(int i = 0; i < 300; i++ ) {			
			
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(j);
			if(i<50)
				cloudlet.setExecStartTime(0);
			if(i>50 && i<100)
				cloudlet.setExecStartTime(1000);
			if(i>100 && i<150)
				cloudlet.setExecStartTime(2000);
			if(i>150 && i<200)
				cloudlet.setExecStartTime(3000);
			if(i>200 && i<250)
				cloudlet.setExecStartTime(4000);
			if(i>250)
				cloudlet.setExecStartTime(5000);
			j=(j+1)%50;	
			// add the cloudlet to the list
			cloudletlist.add(cloudlet);
			
			}
			
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletlist);
			/*
			for(int i = 50; i < 100; i++ ) {			
			
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(j);
			cloudlet.setSubmissionTime(200);
			j=(j+1)%50;	
			// add the cloudlet to the list
			cloudletlist.add(cloudlet);
			
			}
			
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletlist);
			for( int i = 0; i < 300; i++ ) {
				 
					broker.bindCloudletToVm(i,i);
			}
		
			*/
			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			Log.printLine("Problem 4 not completely finished....	");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1200;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList.add(new Pe(1, new PeProvisionerSimple(mips))); //second PE
		
		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048*2; // host memory (MB)
		long storage = 2000000; // host storage
		int bw = 10000;
		
		//set up 100 hosts 
		while(hostId<10000){
		hostList.add(
			new Host(
				hostId++,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerSpaceShared(peList)
			)
		); // This is our machine
		
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

}