package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookExists;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.TakeBook;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.concurrent.CountDownLatch;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private int tick;
	private MoneyRegister register;
	private CountDownLatch latch;


	public SellingService(String name,CountDownLatch latch) {
		super(name);
		register=MoneyRegister.getInstance();
		this.latch=latch;

	}
	/**
	 * subscribes to Tick updates from time service, updates its tick field ,and gracefully terminates upon reciving tick number -1.
	 * as well as Subscribing to handle bookOrderEvent, in which , sends multiple events in order to get said book (if exists) ,and charge the customer (if has enough money).
	 * returns void.
	 */
	@Override @SuppressWarnings("unchecked")
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class,message->{
			this.tick=message.getTick();
			if(tick==-1)
				terminate();
		});

		subscribeEvent(BookOrderEvent.class, message ->{
			int ProcessTick=tick;
			OrderReceipt receipt=null;
			Customer customer=message.getCustomer();
			OrderBook bookToOrder = message.getOrder();
			OrderResult takeResult;
			boolean successfullyCharged=false;

			Future<Integer> bookExistEvent=sendEvent(new BookExists(bookToOrder.getBookTitle()));
			Integer price = new Integer (-1);
			if (bookExistEvent!=null)
				price = bookExistEvent.get();

			if (price !=null && price!=-1 && customer.getAvailableCreditAmount() >= price) { //optimization - won't get into sync if customer does'nt have money to buy the book.
					synchronized (customer.getCreditCard()) { //so we won't be able to charge customer for 2 books when he have money only for one.
						if (customer.getAvailableCreditAmount() >= price) {
							Future<OrderResult> takeBook = sendEvent(new TakeBook(bookToOrder)); //try to take book
							if (takeBook!=null) {
								takeResult = takeBook.get();
								if (takeResult == OrderResult.SUCCESSFULLY_TAKEN) { //if you manage to: charge.
									register.chargeCreditCard(customer, price);
									successfullyCharged = true;
								}
							}
						}
					}
				}

			if(successfullyCharged) {
				receipt = new OrderReceipt(this.getName(), bookToOrder.getBookTitle(), 1, customer.getId(), price, tick, bookToOrder.getTick(), ProcessTick);
				register.file(receipt);
			}
			complete(message,receipt);
		});

		latch.countDown();

	}


}
