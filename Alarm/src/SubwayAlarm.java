import javax.swing.*;
import java.awt.*;
import java.awt.event.*;	

import java.util.*;


import org.json.simple.*;
import org.json.simple.parser.*;

import java.net.*;

import java.io.InputStreamReader;


public class SubwayAlarm extends JFrame implements WindowListener{

	public SubwayAlarm mainFrame;
	public SubwayTrayIcon trayIcon;
	public Alarm alarm;

	public static void main(String[] args){
		new SubwayAlarm();
	}


	String station = null;


	JPanel[] jp = {new JPanel(), new JPanel(), new JPanel(), new JPanel(), new JPanel()};

	JTextField search = new JTextField(10);
	JButton searchButton = new JButton("검색");

	JComboBox<String> direction = new JComboBox<String>();

	JComboBox<String> time = new JComboBox<String>(new String[]{"오전", "오후"});
	JTextField hour = new JTextField(2);
	JTextField minute = new JTextField(2);
	
	JTextField walktime = new JTextField(2);

	JButton checkButton = new JButton("확인");


	JSONArray data = null;

	public SubwayAlarm(){
		super("Title");

		mainFrame = this;

		JPanel p = new JPanel();

		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		checkButton.setEnabled(false);


		jp[0].add(new JLabel("지하철 역"));
		jp[0].add(search);
		jp[0].add(searchButton);
		p.add(jp[0]);


		searchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				station = search.getText();
				

				if(station == null || station.equals("")){
					JOptionPane.showConfirmDialog(mainFrame, "역 이름을 입력하세요.", "에러",JOptionPane.DEFAULT_OPTION ,JOptionPane.ERROR_MESSAGE);
					return;
				}

				data = OpenAPI(station,20);
				
				if(data == null){
					JOptionPane.showConfirmDialog(mainFrame, "존재하지 않는 역입니다.", "에러",JOptionPane.DEFAULT_OPTION ,JOptionPane.ERROR_MESSAGE);
					return;
				}

				ArrayList<String> list = new ArrayList<String>();

				direction.removeAllItems();
				for(int i = 0 ; i < data.size(); i++) {
					JSONObject json = (JSONObject)data.get(i);

					if(json.get("btrainSttus") != null) continue;
					
					
					String str = convertCodetoString(json.get("subwayId").toString()) +" " + json.get("updnLine").toString();
					if(list.indexOf(str) == -1){
						
						list.add(str);
					}
				}
						
				String[] items = new String[list.size()];
				list.toArray(items);
				
    			Arrays.sort(items, String.CASE_INSENSITIVE_ORDER);

				for(int i = 0; i < items.length; ++ i){
					direction.addItem(items[i]);
				}

				checkButton.setEnabled(true);
			}
		});



		jp[1].add(new JLabel("방향"));
		jp[1].add(direction);
		p.add(jp[1]);



		Calendar cal = Calendar.getInstance();
		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);

		time.setSelectedIndex( ((h < 12 ) ? 0 : 1) );
		hour.setText( ((h % 12 == 0) ? 12 : h % 12) + "");
		minute.setText(m + "");


		jp[2].add(new JLabel("열차를 타야하는 시간"));
		jp[2].add(time);
		jp[2].add(hour);
		jp[2].add(new JLabel("시"));
		jp[2].add(minute);
		jp[2].add(new JLabel("분"));
		p.add(jp[2]);

		walktime.setText("0");
		jp[3].add(new JLabel("역까지 걸리는 시간"));
		jp[3].add(walktime);
		jp[3].add(new JLabel("분"));
		p.add(jp[3]);


		jp[4].add(checkButton);
		p.add(jp[4]);

		add(p);


		checkButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Calendar chTime = CheckTime();
				
				if(chTime == null){
					JOptionPane.showConfirmDialog(mainFrame, "잘못입력하였습니다.", "에러",JOptionPane.DEFAULT_OPTION ,JOptionPane.ERROR_MESSAGE);
				}else{
					setVisible(false);
					trayIcon.Show(chTime);

	 				String directionStr = (String) direction.getSelectedItem();

	 				String subwayCode = convertStringtoCode(directionStr.substring(0,directionStr.length() - 3));
	 				String dir = directionStr.substring(directionStr.length() - 2);
	 				int walk = Integer.parseInt(walktime.getText());

	 				if(alarm != null) alarm.interrupt();

					alarm = new Alarm(station, chTime, subwayCode, dir, walk);
					alarm.start();
				}

				
			}
		});




		setSize(400, 300);
		setVisible(true);
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(this);


		trayIcon = new SubwayTrayIcon(this);
		
	}



	public static String convertCodetoString(String str){
		int code = Integer.parseInt(str);
		if(code >= 1001 && code <=1009){
			return code % 1000 + "호선";
		}else{

		}
		return "";
	}

	public static String convertStringtoCode(String str){
		if(str.length() == 3 && str.substring(1).equals("호선")){
			return "100" + Integer.parseInt(str.substring(0,1));
		}
		return null;
	}

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
		int i = JOptionPane.showConfirmDialog(this, "트레이아이콘으로 최소화 시키겠습니까?", "종료", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if( i == 0){
			trayIcon.Show();
		}else if(i == 1){
			System.exit(0);
		}
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}


	public Calendar CheckTime(){
		int h = Integer.parseInt(hour.getText());
		int m = Integer.parseInt(minute.getText());
		if(h > 0 && h <= 12 && m >= 0 && m < 60){
			Calendar cal = Calendar.getInstance();
			
			if(time.getSelectedIndex() == 1 && h != 12) h += 12;

			cal.set(Calendar.HOUR_OF_DAY,h);
			cal.set(Calendar.MINUTE,m);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
			return cal;
		}
		return null;
	}




	public static JSONArray OpenAPI(String station, int size){

		final String LINK = "http://swopenAPI.seoul.go.kr/api/subway/";
		final String APIKEY = "6f48504b6135323338355464695152";
		
		JSONObject json = null;
		
		try{
			URL url = new URL(LINK + APIKEY + "/json/realtimeStationArrival/1/"+size+"/" + URLEncoder.encode(station, "UTF-8"));


			InputStreamReader isr = new InputStreamReader(url.openConnection().getInputStream(), "UTF-8");

			json = (JSONObject)JSONValue.parseWithException(isr);

			
		}catch(Exception e){
			System.out.println(e.toString());
		}
	
		JSONObject errorMsg = (JSONObject) json.get("errorMessage");
		if(errorMsg == null && json.get("code").toString().equals("INFO-200")) return null;

		return (JSONArray) json.get("realtimeArrivalList");
	}

}
