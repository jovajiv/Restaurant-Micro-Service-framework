package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookExists;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.messages.TakeBook;

import java.util.concurrent.CountDownLatch;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private Inventory inventory = Inventory.getInstance();
	private CountDownLatch latch;
	private int tick;

	public InventoryService(String name,CountDownLatch latch) {
		super(name);
		this.latch=latch;
	}
	/**
	 * subscribes to Tick updates from time service, updates its tick field ,and gracefully terminates upon reciving tick number -1.
	 * as well as Subscribing to handle bookexists and TakeBook events, in which , checks specific cost of book(if available in inventory), and take book from inventory , respectfully.
	 * returns void.
	 */
	@Override @SuppressWarnings("unchecked")
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, message->{
			this.tick=message.getTick();
			if(tick==-1)
				terminate();
		});

		subscribeEvent(BookExists.class, message ->{
			Integer price = inventory.checkAvailabiltyAndGetPrice(message.getName());
			complete(message,price);
		});
		subscribeEvent(TakeBook.class, message ->{
			OrderResult result = inventory.take(message.getOrder().getBookTitle());		// result is enum , either SUCCESSFULLY_TAKEN,NOT_IN_STOCK
			complete(message,result);
		});
		latch.countDown();
	}
}
