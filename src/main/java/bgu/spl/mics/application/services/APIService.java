package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private Customer customer;
	private int tick;
	private CountDownLatch latch;

	//~~~~~~~~~~~~~~~~~~~constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	public APIService(Customer customer, CountDownLatch latch) {
		super(customer.getName());
		this.customer=customer;
		this.latch=latch;
		//Future <OrderReceipt> futureObject = (Future<OrderReceipt>)sendEvent(new BookOrderEvent(getName()));
	}
	/**
	 * subscribes to Tick updates from time service, updates its tick field ,and gracefully terminates upon reciving tick number -1.
	 * this MicroService represents a Customer, and is responsible for attempting to aquire books for the Customer at specific ticks Specified by the customer.
	 * returns void.
	 */
	@Override @SuppressWarnings("unchecked")
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, message ->{
			this.tick=message.getTick();
			if(tick==-1)
				terminate();

			Vector<Future<OrderReceipt>> orderEvent=new Vector<>();
			OrderReceipt resolved=null;
			while(( ! customer.getOrderSchedule().isEmpty()) && customer.getOrderSchedule().elementAt(0).getTick()==tick){ //customer still have things to order & they match to the current tick
				OrderBook toOrder = customer.getOrderSchedule().elementAt(0); //if the tick is matches, try to order.
				orderEvent.add(sendEvent(new BookOrderEvent(toOrder,customer))); //open order book event
				customer.getOrderSchedule().remove(0);
			}


			for(Future<OrderReceipt> order:orderEvent){
				if (order != null)
				resolved = order.get(); //suppose to be Order list, or null if cannot be ordered.
				if (resolved!=null) {
					customer.addReceipt(resolved);
					Future<Boolean> DeliveryEvent = sendEvent(new DeliveryEvent(customer));
				}
			}

		});
		latch.countDown();
	}

}
