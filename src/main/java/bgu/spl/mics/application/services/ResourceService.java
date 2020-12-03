package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.GetVehicleEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;


/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private ResourcesHolder resource = ResourcesHolder.getInstance();
	private CountDownLatch latch;
	private ConcurrentLinkedQueue<Future<DeliveryVehicle>> futureVehicles;
	private int tick;


	public ResourceService(String name,CountDownLatch latch) {
		super(name);
		this.latch=latch;
		this.futureVehicles=new ConcurrentLinkedQueue<>();
	}
	/**
	 * subscribes to Tick updates from time service, updates its tick field ,and gracefully terminates upon reciving tick number -1.
	 * as well as Subscribing to handle GetVehicleEvent, in which , attempts to help logistics Service (who requested said vehicle) with aquiring the vehicle from the Resource holder.
	 * also subscribe to ReleaseVehicleEvent, in which , attempts to help logistics Service to return a Vehicle which finished Delivery to the Resource Holder
	 * (one can think of Resource holder as the company's parking lot)
	 */
	@Override @SuppressWarnings("unchecked")
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, message->{
			this.tick=message.getTick();
			if(tick==-1) {
				for (Future<DeliveryVehicle> futureVehicle:futureVehicles){
					if(!futureVehicle.isDone())
						futureVehicle.resolve(null);
				}
				terminate();
			}
		});

		subscribeEvent(GetVehicleEvent.class, message ->{
			Future<DeliveryVehicle> futureVehicle=resource.acquireVehicle();
			if(!futureVehicle.isDone()) {
				futureVehicles.add(futureVehicle);
			}
			complete(message,futureVehicle);
		});

		subscribeEvent(ReleaseVehicleEvent.class, message ->{
			resource.releaseVehicle(message.getVehicle());
			complete(message,true);
		});
		latch.countDown();
	}

}
