package problems;

import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Problem3 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static ArrayList<Host> createHosts(int noOfHosts) {
		
		ArrayList<Host> hostList = new ArrayList<Host>();
		
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1200;

		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		
		int ram = 4096; // host memory (MB)
		long storage = 2000000; // host storage
		int bw = 10000;

		for( int i = 0; i < noOfHosts; i++ ) {
		
			hostList.add(
					new Host(
							i,
							new RamProvisionerSimple(ram),
							new BwProvisionerSimple(bw),
							storage,
							peList,
							new VmSchedulerSpaceShared(peList)
							)
					); 

		}
		
		return hostList;
	
	}
	
	private static List<Cloudlet> createCloudlets(int noOfCloudlets,int brokerId) {
		
		ArrayList<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		
		// Cloudlet properties
		int pesNumber = 1;
		long length = 1440000000;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		for(int i = 0; i < noOfCloudlets; i++ ) {			

			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);					
			cloudlet.setUserId(brokerId);
		
			cloudletList.add(cloudlet);
		}
		
		return cloudletList;
	}
	
	private static ArrayList<Vm> createVms(int noOfVms, int brokerId) {
		
		ArrayList<Vm> vmlist = new ArrayList<Vm>();
		
		// VM description
		int mips = 1000;
		long size = 1024; // image size (MB)
		int ram = 1024; // vm memory (MB)
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		
		for( int i = 0; i < noOfVms; i++ ) {
		// create VM
		Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
		
		vmlist.add(vm);
		
		}
		
	    return vmlist;
	
	}
	

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Log.printLine("Problem 3:");

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
			
			vmlist = createVms(50,broker.getId());

			// submit vms list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			cloudletList = createCloudlets(300,broker.getId());
			
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			Log.printLine("Problem 2 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static Datacenter createDatacenter(String name) {

		List<Host> hostList = new ArrayList<Host>();

		
		hostList = createHosts(10000);
		
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

		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

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
