package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Array;
import java.util.concurrent.*;
import java.util.*;


/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */


public class Inventory  {

	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
    private ConcurrentHashMap<String,BookInventoryInfo> bookInv;
   // private final Object lock = new Object();
    //~~~~~~~~~~~~~~~~~~~Singletone lazy constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	private static class InventoryHolder {
		private static final Inventory inventory=new Inventory();
	}
	private Inventory () { //private constructor - Singleton
		bookInv = new ConcurrentHashMap<>();
	}
    //~~~~~~~~~~~~~~~~~~~methods~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return InventoryHolder.inventory;
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	public void load (BookInventoryInfo[ ] inventory ) {
		for (BookInventoryInfo book:inventory) {
			bookInv.put(book.getBookTitle(),book);
		}
	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	public OrderResult take(String book) {
            BookInventoryInfo bookk=bookInv.get(book);
            if (bookk.getAmountInInventory()>0) { //optimization! wont use sync if amount is less than 0 or this is not the book.
	        synchronized (bookk) { //because iterator gives a snapshot, so cant set!!! need sync here for that!!
                if (bookk.getAmountInInventory() > 0) {
					bookk.setAmount(bookk.getAmountInInventory() - 1);
                    return OrderResult.SUCCESSFULLY_TAKEN;
                }
            }
        }
		return OrderResult.NOT_IN_STOCK;
	}

	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	public int checkAvailabiltyAndGetPrice(String book) {
        BookInventoryInfo bookk = bookInv.get(book);
            if (bookk!=null && bookk.getAmountInInventory()>0) //what if take is happening here?!?!?
                return bookk.getPrice();

		return -1;
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	public void printInventoryToFile(String filename) {
		HashMap<String, Integer> inventoryHash = new HashMap<>();
		for (String bookTitle : bookInv.keySet()) {
			BookInventoryInfo book = bookInv.get(bookTitle);
			inventoryHash.put(bookTitle, book.getAmountInInventory());

		}
		try (FileOutputStream outputStream = new FileOutputStream(filename); ObjectOutput out = new ObjectOutputStream(outputStream)) {

			out.writeObject(inventoryHash);
		}  catch (IOException e) {
			e.printStackTrace();
		}



	}

}
