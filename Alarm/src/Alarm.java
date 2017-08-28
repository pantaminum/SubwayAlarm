import java.util.*;

import org.json.simple.*;

import java.io.*;
import javax.sound.sampled.*;

public class Alarm extends Thread{

	private String station;
	private Calendar time;
	private String lineid;
	private String direction;
	private int walktime;


	public Alarm(String station, Calendar time, String lineid, String direction, int walktime){
		this.station = station;
		this.time = time;
		this.lineid = lineid;
		this.direction = direction;
		this.walktime = walktime;
	}


	public void run(){
		
		while(!Thread.currentThread().isInterrupted()){
			Calendar now = Calendar.getInstance();
			long restTime = time.getTimeInMillis() - now.getTimeInMillis();
			System.out.println("restTime = " + restTime + "ms");
			if(restTime <= 20 * 60 * 1000) break;

			try{
				Thread.sleep(60*1000);
			}catch(InterruptedException e){
				return;
			}catch(Exception e){}
		}

		JSONArray json = SubwayAlarm.OpenAPI(station, 20);


		long prevTime = -1;
		for(int i = 0; i < json.size(); ++ i){
			JSONObject data = (JSONObject) json.get(i);
			
			if(data.get("subwayId").toString().equals(lineid) && data.get("updnLine").toString().equals(direction)){
				int subwayTime = Integer.parseInt(data.get("barvlDt").toString());

				if(subwayTime > walktime * 60){
					Calendar subwayCalendar = Calendar.getInstance();
					subwayCalendar.add(Calendar.SECOND, subwayTime);

					System.out.println("arrive time : " + subwayTime + "s");

					if(time.getTimeInMillis() < subwayCalendar.getTimeInMillis()){
						Ring(prevTime);
						return;
					}
					prevTime = subwayTime;
				}

			}
		}

		Ring(prevTime);
	}

	public boolean Ring(long time){
		if(time == -1) return false;
		time -= walktime * 60;
		System.out.println("Alarm : " + time + "s");
		try{
			Thread.sleep( time * 1000 );
			

			Sound();

			System.out.println("end");

		}catch(InterruptedException e){
			System.out.println(e.toString());
		}catch(Exception e){
			System.out.println(e.toString());
 		}

		return true;
	}

	public void Sound(){
		System.out.println("Sound");
		try{

			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getClass().getResource("sound.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
			

		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
}