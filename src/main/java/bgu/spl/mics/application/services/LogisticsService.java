package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.GetVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;

import java.util.concurrent.CountDownLatch;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~

	private CountDownLatch latch;
	private int tick;

	public LogisticsService(String name,CountDownLatch latch) {
		super(name);

		this.latch=latch;
	}

	/**
	 * subscribes to Tick updates from time service, updates its tick field ,and gracefully terminates upon reciving tick number -1.
	 * as well as Subscribing to handle DeliveryEvent, in which , attempts to aquiere a vehicle for said Delivery, "Sleeping" during Delivery time , and releasing the vehicle back to the Company parking lot at the end.
	 * returns void.
	 */
	@Override @SuppressWarnings("unchecked")
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, message->{
			this.tick=message.getTick();
			if(tick==-1)
				terminate();
		});

		subscribeEvent(DeliveryEvent.class, message ->{
			DeliveryVehicle vehicle=null;
			Future<Future<DeliveryVehicle>> DeliveryEvent = sendEvent(new GetVehicleEvent());
			if(DeliveryEvent!=null) {
				Future<DeliveryVehicle> devEvent=DeliveryEvent.get();
				if(devEvent!=null)
				vehicle = devEvent.get();
				if (vehicle != null) {
					vehicle.deliver(message.getCustomer().getAddress(), message.getCustomer().getDistance());
					complete(message, true);
					Future<Boolean> ReleaseEvent = sendEvent(new ReleaseVehicleEvent(vehicle));
				} else {
					complete(message, false);
				}
			}
			else
				complete(message,false);
		});
		latch.countDown();

	}

}
