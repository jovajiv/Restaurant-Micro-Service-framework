package bgu.spl.mics;




import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;



/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */



public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<Class<? extends Event>,ConcurrentLinkedQueue<MicroService>> eventToMicroHandlers;	// each event has a list of Microservices who subscribed to it
	private ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedQueue<MicroService>> broadcastToMicroServices;  //Each Broadcast has a list of Microservices who subscribed to it.
	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class>> microToEvent; // queue of both Broadcasts and events, Main use is unregister function.
	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Message>> microToQueue;  // Each MicroService has message queue.

	private ConcurrentHashMap<Event,Future> EventToFuture;

	private static class MessageBusImplHolder { //lazy singletone as shown in PS
		private static final MessageBusImpl MessageBusImpl=new MessageBusImpl();
	}

	private MessageBusImpl(){ //private constructor
		this.eventToMicroHandlers=new ConcurrentHashMap<>();
		this.broadcastToMicroServices=new ConcurrentHashMap<>();
		this.microToQueue=new ConcurrentHashMap<>();
		this.microToEvent=new ConcurrentHashMap<>();
		this.EventToFuture=new ConcurrentHashMap<>();
	}

	public static  MessageBusImpl getInstance(){
		return MessageBusImplHolder.MessageBusImpl;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventToMicroHandlers.putIfAbsent(type,new ConcurrentLinkedQueue<>());
		eventToMicroHandlers.get(type).add(m);
		microToEvent.putIfAbsent(m,new ConcurrentLinkedQueue<>());
		microToEvent.get(m).add(type);

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastToMicroServices.putIfAbsent(type,new ConcurrentLinkedQueue<>());
		broadcastToMicroServices.get(type).add(m);
		microToEvent.putIfAbsent(m,new ConcurrentLinkedQueue<>());
		microToEvent.get(m).add(type);

	}

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		Future future=EventToFuture.get(e);
		EventToFuture.remove(e);
		future.resolve(result);

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> vec = null;
		if(broadcastToMicroServices.get(b.getClass())!=null) {
				vec = broadcastToMicroServices.get(b.getClass());
					for (MicroService ms:vec) {			///https://stackoverflow.com/questions/17197960/concurrentmodificationexception-when-printing-the-contents-of-a-vector    happends when running debug on broadcast commands///
						synchronized (microToQueue.get(ms)) {
							microToQueue.get(ms).add(b);
							microToQueue.get(ms).notify();        // used to notify awaitMessage that there is a message in queue for him. , notifies on Personal queue object belonging to specific microservice.

						}
					}
		}
	}

	// when enqueuing and dequinging we need to sync becouse 2 services might access to the round robin mech at the same time for the same event,
	@Override @SuppressWarnings("unchecked")
	public <T> Future<T> sendEvent(Event<T> e) {
		MicroService ms=null;
		ConcurrentLinkedQueue<MicroService> queue=null;
		Future future=null;									// init to null in case no suitable Microservice handler was found.
		if(eventToMicroHandlers.get(e.getClass())!=null)
		synchronized (eventToMicroHandlers.get(e.getClass())) {        // lock on individual event queue only.
			queue = eventToMicroHandlers.get(e.getClass());
			if (queue != null) {  //else //this Event does not exist, Meaning no microservice handler ever subscribed to handle this event , reaching here means fatal error.
				ms=queue.poll();
				if (ms!=null){  //else //No Microservices to handle this event were found, meaning microservices did subscribe to handle this event, but they were removed using the unsubscribe function.
					queue.add(ms);
				}
			}
		}

		if(ms!=null){
			future=new Future();
			EventToFuture.put(e,future);
			if(microToQueue.get(ms)!=null) {
				synchronized (microToQueue.get(ms)) {
					if(microToQueue.get(ms)!=null) {
						microToQueue.get(ms).add(e);
						microToQueue.get(ms).notify();        // used to notify awaitMessage that there is a message in queue for him. , notifies on Personal queue object belonging to specific microservice.
					}
					else
						return null;
				}
			}
			else
				return null;

		}
		return future;

	}

	@Override
	//implemented using putIfAbsent and not using put becouse this is a public method , anyone can access it , do not want someone to overrun it.
	public void register(MicroService m) {
		microToQueue.putIfAbsent(m,new ConcurrentLinkedQueue<>());
	}

	@Override @SuppressWarnings("unchecked")
	public void unregister(MicroService m) {
		synchronized (microToQueue.get(m)){
			unsubscribeAll(m);
			for(Message msg:microToQueue.get(m)) {
				if (msg instanceof Event) {
					complete((Event) msg, null);        //According to instructions given in forum.
				}
			}
			microToQueue.remove(m);		// unregister the personal queue
		}

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		ConcurrentLinkedQueue<Message> queue=microToQueue.get(m);
		synchronized (queue) {
			while (queue.peek() == null) {			// if we get a notified (by send event function) after the while condition is checked, and before the queue.wait is issued, then we miss the notification sent by send event ,therefor we will wait for message while one is already in the queue.
				queue.wait();
			}
		}
		return queue.poll();
	}
	// question , what about removing from the Event to Future mappings ? it sounds like those will stay ....
	// unsubscribes from events and broadcasts previouslly subscribed to.
	private void unsubscribeAll(MicroService m){
		ConcurrentLinkedQueue<Class> queue=microToEvent.get(m);
		if(queue!=null)
		for(Class cla:queue){
			if(Broadcast.class.isAssignableFrom(cla)){		// checks if broadcast is super/interface of cla (meaning cla implements/extends broadcast)
					broadcastToMicroServices.get(cla).remove(m);
			}
			else{
					eventToMicroHandlers.get(cla).remove(m);
			}
		}
	}



}
