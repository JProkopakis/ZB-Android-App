package com.example.myapplication;
import android.app.Activity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import zephyr.android.BioHarnessBT.*;

public class NewConnectedListener extends ConnectListenerImpl
{
	public JSONObject jsonObj = new JSONObject();
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
	private Handler _OldHandler;
	private Handler _aNewHandler; 
	final int GP_MSG_ID = 0x20;
	final int BREATHING_MSG_ID = 0x21;
	final int ECG_MSG_ID = 0x22;
	final int RtoR_MSG_ID = 0x24;
	final int ACCEL_100mg_MSG_ID = 0x2A;
	final int SUMMARY_MSG_ID = 0x2B;
	
	
	private int GP_HANDLER_ID = 0x20;
	
	private final int HEART_RATE = 0x100;
	private final int RESPIRATION_RATE = 0x101;
	private final int SKIN_TEMPERATURE = 0x102;
	private final int POSTURE = 0x103;
	private final int PEAK_ACCLERATION = 0x104;
	private final int BATTERY_STATUS = 0x105;
	private final int WORN_STATUS = 0x106;
	private final int ACTIVITY = 0x107;
	private final int HR_CONF = 0x108;
	/*Creating the different Objects for different types of Packets*/
	private GeneralPacketInfo GPInfo = new GeneralPacketInfo();
	private ECGPacketInfo ECGInfoPacket = new ECGPacketInfo();
	private BreathingPacketInfo BreathingInfoPacket = new  BreathingPacketInfo();
	private RtoRPacketInfo RtoRInfoPacket = new RtoRPacketInfo();
	private AccelerometerPacketInfo AccInfoPacket = new AccelerometerPacketInfo();
	private SummaryPacketInfo SummaryInfoPacket = new SummaryPacketInfo();
	
	private PacketTypeRequest RqPacketType = new PacketTypeRequest();
	public NewConnectedListener(Handler handler,Handler _NewHandler) {
		super(handler, null);
		_OldHandler= handler;
		_aNewHandler = _NewHandler;

		// TODO Auto-generated constructor stub

	}
	public void Connected(ConnectedEvent<BTClient> eventArgs) {
		System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
		/*Use this object to enable or disable the different Packet types*/
		RqPacketType.GP_ENABLE = true;
		RqPacketType.BREATHING_ENABLE = true;
		RqPacketType.LOGGING_ENABLE = true;
		RqPacketType.SUMMARY_ENABLE = true;
		
		
		//Creates a new ZephyrProtocol object and passes it the BTComms object
		ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), RqPacketType);
		//ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
		_protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
			public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
				ZephyrPacketArgs msg = eventArgs.getPacket();
				byte CRCFailStatus;
				byte RcvdBytes;
				
				
				
				CRCFailStatus = msg.getCRCStatus();
				RcvdBytes = msg.getNumRvcdBytes() ;
				int MsgID = msg.getMsgID();
				byte [] DataArray = msg.getBytes();

				switch (MsgID)
				{

				case GP_MSG_ID:

					String timestamp =  GPInfo.GetTSYear(DataArray) + String.format("%02d", GPInfo.GetTSMonth(DataArray)) + String.format("%02d", GPInfo.GetTSDay(DataArray)) +" "+ String.format("%02d", GPInfo.GetMsofDay(DataArray)/1000/60/60)+":"+String.format("%02d", GPInfo.GetMsofDay(DataArray)%(1000*60*60)/(1000*60))+":"+String.format("%02d", GPInfo.GetMsofDay(DataArray)%(1000*60)/(1000))+":"+String.format("%03d", GPInfo.GetMsofDay(DataArray)%(1000));
					
					
					
					//***************Displaying the Heart Rate********************************
					int HRate =  GPInfo.GetHeartRate(DataArray);
					Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
					Bundle b1 = new Bundle();
					b1.putString("HeartRate", String.valueOf(HRate));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Heart Rate is "+ HRate);

					//***************Displaying the Respiration Rate********************************
					double RespRate = GPInfo.GetRespirationRate(DataArray);
					
					text1 = _aNewHandler.obtainMessage(RESPIRATION_RATE);
					b1.putString("RespirationRate", String.valueOf(RespRate));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Respiration Rate is "+ RespRate);

					//***************Displaying the Activity********************************
					double Activity = GPInfo.GetVMU(DataArray);

					text1 = _aNewHandler.obtainMessage(ACTIVITY);
					b1.putString("Activity", String.valueOf(Activity));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Activity is "+ Activity);
					
					//***************Displaying the Skin Temperature*******************************
		

					double SkinTempDbl = GPInfo.GetSkinTemperature(DataArray);
					 text1 = _aNewHandler.obtainMessage(SKIN_TEMPERATURE);
					//Bundle b1 = new Bundle();
					b1.putString("SkinTemperature", String.valueOf(SkinTempDbl));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Skin Temperature is "+ SkinTempDbl);
					
					//***************Displaying the Posture******************************************					

					int PostureInt = GPInfo.GetPosture(DataArray);
					text1 = _aNewHandler.obtainMessage(POSTURE);
					b1.putString("Posture", String.valueOf(PostureInt));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Posture is "+ PostureInt);
					//***************Displaying the Peak Acceleration******************************************

					double PeakAccDbl = GPInfo.GetPeakAcceleration(DataArray);
					text1 = _aNewHandler.obtainMessage(PEAK_ACCLERATION);
					b1.putString("PeakAcceleration", String.valueOf(PeakAccDbl));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Peak Acceleration is "+ PeakAccDbl);

					byte ROGStatus = GPInfo.GetROGStatus(DataArray);
					System.out.println("ROG Status is "+ ROGStatus);


					try {
						jsonObj.put("HR", String.valueOf(HRate));
						jsonObj.put("BR", String.valueOf(RespRate));
						jsonObj.put("Posture", String.valueOf(PostureInt));
						jsonObj.put("Activity", String.valueOf(GPInfo.GetVMU(DataArray)));
						jsonObj.put("Acceleration", String.valueOf(PeakAccDbl));
						jsonObj.put("XMin", String.valueOf(GPInfo.GetX_AxisAccnMin(DataArray)));
						jsonObj.put("XPeak", String.valueOf(GPInfo.GetX_AxisAccnPeak(DataArray)));
						jsonObj.put("YMin", String.valueOf(GPInfo.GetY_AxisAccnMin(DataArray)));
						jsonObj.put("YPeak", String.valueOf(GPInfo.GetY_AxisAccnPeak(DataArray)));
						jsonObj.put("ZMin", String.valueOf(GPInfo.GetZ_AxisAccnMin(DataArray)));
						jsonObj.put("ZPeak", String.valueOf(GPInfo.GetZ_AxisAccnPeak(DataArray)));
						jsonObj.put("phone_time", simpleDateFormat.format(new Date(System.currentTimeMillis())));
						jsonObj.put("timestamp", timestamp);

					}catch (JSONException e){
						e.printStackTrace();
					}


					//***************Displaying the Battery******************************************
					int BatteryInt = GPInfo.GetBatteryStatus(DataArray);
					text1 = _aNewHandler.obtainMessage(BATTERY_STATUS);
					b1.putString("Battery", String.valueOf(BatteryInt)+"%");
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Battery is "+ BatteryInt+"%");

					//***************Displaying the WornStatus******************************************
					int wornInt = GPInfo.GetWornStatus(DataArray);
					text1 = _aNewHandler.obtainMessage(WORN_STATUS);
					b1.putString("isWorn", String.valueOf(wornInt));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("worn status is "+ wornInt);


					break;


				case BREATHING_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					System.out.println("Breathing Packet Sequence Number is "+BreathingInfoPacket.GetSeqNum(DataArray));
					break;
				case ECG_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					System.out.println("ECG Packet Sequence Number is "+ECGInfoPacket.GetSeqNum(DataArray));
					break;
				case RtoR_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					System.out.println("R to R Packet Sequence Number is "+RtoRInfoPacket.GetSeqNum(DataArray));
					break;
				case ACCEL_100mg_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					System.out.println("Accelerometry Packet Sequence Number is "+AccInfoPacket.GetSeqNum(DataArray));
					break;
				case SUMMARY_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
					System.out.println("Summary Packet Sequence Number is "+SummaryInfoPacket.GetSeqNum(DataArray));

					int HRconf = SummaryInfoPacket.GetHeartRateRateConfidence(DataArray);
					Message text2 = _aNewHandler.obtainMessage(HR_CONF);
					Bundle b2 = new Bundle();
					b2.putString("HeartRateConfidence", String.valueOf(HRconf));
					text2.setData(b2);
					_aNewHandler.sendMessage(text2);
					System.out.println("Heart Rate Confidence is "+ HRconf);
					break;
					
				}
			}
		});
	}
	
}