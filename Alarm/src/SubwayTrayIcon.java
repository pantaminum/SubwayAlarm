import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;
import javax.imageio.ImageIO;
import java.io.File;


import java.awt.SystemTray;

public class SubwayTrayIcon implements Runnable{

	Calendar time = null;
	SubwayAlarm mainFrame;


	public SubwayTrayIcon(SubwayAlarm parent){
		mainFrame = parent;
	}

	public void Show(){
		Show(null);
	}
	public void Show(Calendar time){
		this.time = time;
		EventQueue.invokeLater(this);
	}

	public void run(){
		if(SystemTray.isSupported()){

			try{
				SystemTray tray = SystemTray.getSystemTray();
				PopupMenu popup = new PopupMenu();
				

				// Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
				Image image = ImageIO.read(new File("icon.png"));
				TrayIcon trayIcon = new TrayIcon(image, "Teh Tip Text", popup);

				MenuItem item;

				if(time != null){
					item = new MenuItem(time.get(Calendar.HOUR_OF_DAY) + "시 " + time.get(Calendar.MINUTE)+ "분");
					popup.add(item);
				}

				item = new MenuItem("설정 창 보기");
				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						mainFrame.setVisible(true);
						Stop(trayIcon);
					}
				});
				popup.add(item);



				if(time != null){
					item = new MenuItem("알람 끄기");
					item.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e){
							mainFrame.alarm.interrupt();
						}
					});
					popup.add(item);
				}



				item = new MenuItem("종료");

				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						System.exit(0);
					}
				});

				popup.add(item);
				tray.add(trayIcon);
			}catch(Exception e){}

		}else{
			System.err.println("Tray unavailable");
		}
	}

	public void Stop(TrayIcon trayIcon){
		try{
			SystemTray tray = SystemTray.getSystemTray();
			tray.remove(trayIcon);
		}catch(Exception e){}
	}
}