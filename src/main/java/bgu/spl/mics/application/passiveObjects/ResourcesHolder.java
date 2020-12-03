package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private ConcurrentLinkedQueue<DeliveryVehicle> vehicles;
	private ConcurrentLinkedQueue<Future<DeliveryVehicle>> futureVehicles;
	private final Object lock = new Object();

	//~~~~~~~~~~~~~~~~~~~Singletone lazy constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	private static class ResourcesHolderLazy {
		private static final ResourcesHolder Resource=new ResourcesHolder();
	}
	private ResourcesHolder () { //private constructor - Singleton
		vehicles = new ConcurrentLinkedQueue<>();
		futureVehicles=new ConcurrentLinkedQueue<>();
	}
	//~~~~~~~~~~~~~~~~~~~methods~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {
		return ResourcesHolderLazy.Resource;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> future= new Future<>();
		synchronized (vehicles){
			if(!vehicles.isEmpty()){
				future.resolve(vehicles.poll());
				return future;
			}
		}
		synchronized (futureVehicles) {
			futureVehicles.add(future);
		}
		return future;


	}


	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
	 *     added an option to pass null vehicle as paramter, only used for gracefull termination of the program (this is the only function with the ability to "NotifyALL" on the vehicls queue.
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		if(vehicle!=null)
			synchronized (futureVehicles) {
				if (futureVehicles.isEmpty())    // no one waits for a vehicle
					vehicles.add(vehicle);        // re-add to orig list
				else
					futureVehicles.poll().resolve(vehicle);
			}
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		for (DeliveryVehicle dv:vehicles) {
			this.vehicles.add(dv);
		}
	}

}
