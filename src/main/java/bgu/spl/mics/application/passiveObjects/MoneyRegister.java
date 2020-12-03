package bgu.spl.mics.application.passiveObjects;



import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;


/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class MoneyRegister implements Serializable {
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private CopyOnWriteArrayList<OrderReceipt> orderReceiptsList;
	private AtomicInteger amount;
	//~~~~~~~~~~~~~~~~~~~Singletone lazy constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	private static class MoneyRegisterHolder {
		private static final MoneyRegister moneyReg=new MoneyRegister();
	}
	private MoneyRegister () { //private constructor - Singleton
		orderReceiptsList = new CopyOnWriteArrayList<>();
		amount =  new AtomicInteger(0);
	}
	//~~~~~~~~~~~~~~~~~~~methods~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
     * Retrieves the single instance of this class.
     */
	public static MoneyRegister getInstance() {
		return MoneyRegisterHolder.moneyReg;
	}
	
	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */
	public void file (OrderReceipt r) {
			orderReceiptsList.add(r);
			amount.addAndGet(r.getPrice());
	}
	
	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings() {

		return amount.get();
	}
	
	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */
	public void chargeCreditCard(Customer c, int amount) {
		synchronized (c) { //sync so won't be able to double charge. sund is only on the customer itself - "cheap" sync.
			c.chargeCreditsAmount(amount);
		}
	}

	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.. 
     */
	public void printOrderReceipts(String filename) {
		try (FileOutputStream outputStream = new FileOutputStream(filename); ObjectOutput out = new ObjectOutputStream(outputStream)) {

			out.writeObject(orderReceiptsList);
		}  catch (IOException e) {
			e.printStackTrace();
		}

	}

}
