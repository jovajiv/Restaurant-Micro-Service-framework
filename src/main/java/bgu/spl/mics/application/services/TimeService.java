package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private int speed;
	private int duration;

	public TimeService(int speed, int duration) {
		super("time");
		this.speed=speed;
		this.duration=duration;
	}


	public int getDuration() {
		return duration;
	}

	public int getSpeed() {
		return speed;
	}
	/**
	 * runs for "duration" amount of Ticks, Tick speed indicates amount of time passed between ticks
	 * after "speed" amount of time, this Thread sends broadcast to update everyone who subscribed about the tick change.
	 * whend "Duration" amount is over, sends -1 Value to indicate its time to Gracefully Terminate the program.
	 */
	@Override
	protected void initialize() {

		int tempDuration=1;
		while (duration+1!=tempDuration){
			sendBroadcast(new TickBroadcast(tempDuration));
			try {
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tempDuration++;

		}
		sendBroadcast(new TickBroadcast(-1));

		terminate();
		
	}

}
