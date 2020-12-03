package bgu.spl.mics.application.passiveObjects;
import java.io.Serializable;
import java.util.*;


import java.util.LinkedList;
import java.util.List;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private int id, distance;
	private String name, address;
	private CreditCard creditCard;
	private Vector<OrderBook> orderSchedule;
	private List<OrderReceipt> Receipts;
	//~~~~~~~~~~~~~~~~~~~constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	public Customer (int id, String name,String address, int distance, CreditCard creditCard, Vector<OrderBook> orderSchedule) {
		this.id = id;
		this.distance = distance;
		this.creditCard = creditCard;
		this.name = name;
		this.address = address;
		this.orderSchedule=orderSchedule;
		Collections.sort(orderSchedule);
		this.Receipts = new LinkedList<OrderReceipt>();


	}
	//~~~~~~~~~~~~~~~~~~~methods~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Retrieves the name of the customer.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the ID of the customer  .
	 */
	public int getId() {
		return id;
	}

	/**
	 * Retrieves the address of the customer.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Retrieves the distance of the customer from the store.
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * Retrieves a list of receipts for the purchases this customer has made.
	 * <p>
	 * @return A list of receipts.
	 */
	public List<OrderReceipt> getCustomerReceiptList() {
		return Receipts;
	}

	/**
	 * Retrieves the amount of money left on this customers credit card.
	 * <p>
	 * @return Amount of money left.
	 */
	public int getAvailableCreditAmount() {
		return creditCard.getAmount() ;
	}

	/**
	 * Retrieves this customers credit card serial number.
	 */
	public int getCreditNumber() {
		return creditCard.getNumber();
	}

	public void chargeCreditsAmount (int amount) {
		this.creditCard.chargeAmount(amount);
	}

	public Vector<OrderBook> getOrderSchedule(){
		return orderSchedule;
	}

	public void addReceipt(OrderReceipt receipt){
		Receipts.add(receipt);
	}

	public CreditCard getCreditCard() {
		return creditCard;
	}




}
