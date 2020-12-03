package bgu.spl.mics.application;

import bgu.spl.mics.application.services.LogisticsService;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.lang.Thread;



/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner  {
    public static void main(String[] args) {
        long startTime=System.currentTimeMillis();


        if (args.length == 5 || args.length==6) { //added by shai ->now we will get in even if there are 6 arguments.
            String inJsonPath = args[0];
            String outJsonCustomers = args[1];
            String outJsonBooks = args[2];
            String outJsonReceipts = args[3];
            String outJsonMoneyRegister = args[4];
            JsonParser parser = new JsonParser();
            JsonObject element = null;
            Vector<Thread> threads=null;
            Vector<Customer> customers=null;



            try (JsonReader reader = new JsonReader(new FileReader(inJsonPath)) ) {
                element = (JsonObject) parser.parse(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (element != null) {
                Jsonloaders(element);
                JsonObject services = element.get("services").getAsJsonObject();
                int sellingThreadAmount = services.get("selling").getAsInt();
                int inventoryThreadAmount = services.get("inventoryService").getAsInt();
                int logisticsThreadAmount = services.get("logistics").getAsInt();
                int resourceThreadAmount = services.get("resourcesService").getAsInt();
                TimeService time=ParseTime(services);
                customers=ParseCustomers(services);
                threads=run(sellingThreadAmount,inventoryThreadAmount,logisticsThreadAmount,resourceThreadAmount,customers,time);
            }

            if(threads!=null)
                for (Thread thread:threads){
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
                }
            print( outJsonCustomers,  outJsonBooks,  outJsonReceipts,  outJsonMoneyRegister, customers);
        }
        else {
            System.out.println("Not Enough Parameters, Terminating.");
        }
    }
    /**
     * Receives JsonObject called services, this function parses all customers Data, parses each one , and calls the "Customer" constructor with data collected from Json.
     * returns Vector of all customer Objects created from Json.
     * <p>
     */
    private static Vector<Customer>ParseCustomers(JsonObject services) {
        Gson gson = new GsonBuilder().create();
        JsonArray jsoncustomers = services.get("customers").getAsJsonArray();
        Vector<Customer> customers = new Vector<>();
        for (JsonElement customer : jsoncustomers) {
            int id = customer.getAsJsonObject().get("id").getAsInt();
            String name = customer.getAsJsonObject().get("name").getAsString();
            String address = customer.getAsJsonObject().get("address").getAsString();
            int distance = customer.getAsJsonObject().get("distance").getAsInt();
            JsonObject credit = customer.getAsJsonObject().get("creditCard").getAsJsonObject();
            CreditCard creditCard = gson.fromJson(credit, CreditCard.class);
            Vector<OrderBook> orderSchedule = new Vector<>();
            JsonArray sched = customer.getAsJsonObject().get("orderSchedule").getAsJsonArray();
            for (JsonElement order : sched) {
                orderSchedule.add(gson.fromJson(order.getAsJsonObject(), OrderBook.class));
            }
            Customer customerObject = new Customer(id, name, address, distance, creditCard, orderSchedule);
            customers.add(customerObject);
        }
        return customers;
    }

    /**
     * Receives JsonObject called services, this function parses Time object under "Services" , Calls TimeService constructor to create object.
     * returns TimeService object
     * <p>
     */
    private static TimeService ParseTime(JsonObject services){
        int duration = services.get("time").getAsJsonObject().get("duration").getAsInt();
        int speed = services.get("time").getAsJsonObject().get("speed").getAsInt();
        TimeService time = new TimeService(speed, duration);
        return time;
    }
    /**
     * receives the entire Json file preloaded into "element" , reads lists of  books and vehicles, then loads them to relevent passive objects responsible for holding said lists.
     * returns void
     * <p>
     */
    private static void Jsonloaders(JsonObject element){
        Gson gson = new GsonBuilder().create();
        JsonElement responseWrapper = element.get("initialInventory");
        BookInventoryInfo[] books = gson.fromJson(responseWrapper, BookInventoryInfo[].class);

        JsonElement jsonVehicles = element.get("initialResources").getAsJsonArray().get(0).getAsJsonObject().get("vehicles");
        DeliveryVehicle[] vehicles = gson.fromJson(jsonVehicles, DeliveryVehicle[].class);


        Inventory.getInstance().load(books);
        ResourcesHolder.getInstance().load(vehicles);
    }

    /**
     * receives amount of various Threads needed to be created, as well as Timeservice object and Customer Vector.
     * created all the Threads requested by the system (according to amount of threads requested, and size of customer vector).
     * the TimeService Thread will be created only after all other Threads finished initializing.
     * returns Vector<Thread> containing all the threads created.
     * <p>
     */
    private static Vector<Thread> run(int sellingThreadAmount,int inventoryThreadAmount, int logisticsThreadAmount,int resourceThreadAmount,Vector<Customer> customers,TimeService time){
        int amountThread=sellingThreadAmount+inventoryThreadAmount+logisticsThreadAmount+resourceThreadAmount+customers.size();
        HashMap<Integer,Customer> customerHashMap=new HashMap<>();

        CountDownLatch latch=new CountDownLatch(amountThread);
        Vector<Thread> threads=new Vector<>();
        threads.addAll(runThreads("sellingService", sellingThreadAmount,latch));
        threads.addAll(runThreads("inventoryService", inventoryThreadAmount,latch));
        threads.addAll(runThreads("logisticsService", logisticsThreadAmount,latch));
        threads.addAll(runThreads("resourcesService", resourceThreadAmount,latch));

        for (Customer customer:customers) {
            Thread t1=new Thread(new APIService(customer,latch));
            t1.setName("customer "+customer.getName());
            t1.start();
            threads.add(t1);
        }
        try {
            latch.await();
            new Thread(time).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return threads;

    }

    /**
     * receives amount of various Threads needed to be created, creates them.
     * returns Vector<Thread> containing all the threads created.
     * <p>
     */
    private static Vector<Thread> runThreads(String type, int amount, CountDownLatch latch) {
        Vector<Thread> threads=new Vector<>();
        switch (type) {
            case "sellingService":
                for (int i = 1; i <= amount; i++) {
                    Thread t1= new Thread(new SellingService("selling "+i,latch));
                    t1.setName("selling "+i);
                    t1.start();
                    threads.add(t1);


                }
                break;
            case "inventoryService":
                for (int i = 1; i <= amount; i++) {
                    Thread t1= new Thread(new InventoryService("inventory "+i,latch));
                    t1.setName("inventory "+i);
                    t1.start();
                    threads.add(t1);
                }
                break;
            case "logisticsService":
                for (int i = 1; i <= amount; i++) {
                    Thread t1= new Thread(new LogisticsService("logistics "+i,latch));
                    t1.setName("logistics "+i);
                    t1.start();
                    threads.add(t1);
                }
                break;
            case "resourcesService":
                for (int i = 1; i <= amount; i++) {
                    Thread t1= new Thread(new ResourceService("resource "+i,latch));
                    t1.setName("resource "+i);
                    t1.start();
                    threads.add(t1);
                }
                break;
        }
        return threads;
    }



    /**
     * recieves 4 Strings and Customer Vector.
     * Each string is a System Path to output file.
     * prints customerHashMap to outJsonCustomers path
     * prints MoneyRegister to outJsonMoneyRegister path
     * prints Inventory to out outJsonBooks path
     * prints MoneyRegister.Receipts to  outJsonReceipts path
     * returns void
     * <p>
     */
    private static void print(String outJsonCustomers, String outJsonBooks, String outJsonReceipts, String outJsonMoneyRegister,Vector<Customer> customers){

        HashMap<Integer,Customer> customerHashMap=new HashMap<>();
        for (Customer customer:customers) {
            customerHashMap.put(customer.getId(), customer);
        }
        try (FileOutputStream outputStream = new FileOutputStream(outJsonCustomers); ObjectOutput out = new ObjectOutputStream(outputStream)) {
            out.writeObject(customerHashMap);
        }  catch (IOException e) {
            e.printStackTrace();
        }


        try (FileOutputStream outputStream = new FileOutputStream(outJsonMoneyRegister); ObjectOutput out = new ObjectOutputStream(outputStream)) {

            out.writeObject(MoneyRegister.getInstance());
        }  catch (IOException e) {
            e.printStackTrace();
        }


        Inventory.getInstance().printInventoryToFile(outJsonBooks);
        MoneyRegister.getInstance().printOrderReceipts(outJsonReceipts);
    }
}


