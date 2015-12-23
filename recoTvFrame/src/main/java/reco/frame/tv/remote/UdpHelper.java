package reco.frame.tv.remote;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import android.os.Handler;
import android.util.Log;

public class UdpHelper {
	private final static String TAG = "UdpHelper";
	public static UdpHelper helper;
	private Handler handler = new Handler();
	private MulticastSocket ms;
	private MulticastSocket ds;
	String multicastHost = "224.0.0.1";
	InetAddress receiveAddress;
	private boolean flag=true;

	public static UdpHelper instance() {

		synchronized (UdpHelper.class) {

			if (helper == null) {
				helper = new UdpHelper();
			}

		}
		return helper;

	}

	public UdpHelper() {
		// try {
		// ms = new MulticastSocket();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		try {
			ds = new MulticastSocket(Remote.TV_SERVER_PORT);
			receiveAddress = InetAddress.getByName(multicastHost);
			ds.joinGroup(receiveAddress);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public void send(final String msg) {
		new Thread() {
			@Override
			public void run() {

				DatagramPacket dataPacket = null;
				try {
					ds.setTimeToLive(4);
					// 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
					byte[] data = msg.getBytes();
					// 224.0.0.1为广播地址
					InetAddress address = InetAddress.getByName("224.0.0.1");
					// 这个地方可以输出判断该地址是不是广播类型的地址
					dataPacket = new DatagramPacket(data, data.length, address,
							Remote.TV_SERVER_PORT);
					ds.send(dataPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				super.run();
			}
		}.start();
	}

	public void startListen(final OnUdpListener listener) {

		flag=true;
		new Thread() {
			public void run() {
				try {
					
					Log.i(TAG, "start udp listen");
					byte buf[] = new byte[1024];
					DatagramPacket dp = new DatagramPacket(buf, 1024);
					while (flag) {
						try {
							ds.receive(dp);
							final String result = new String(buf, 0,
									dp.getLength()).trim();
							Log.i(TAG, result);
							if (!result.isEmpty()) {
								handler.post(new Runnable() {

									@Override
									public void run() {
										listener.onMessageReceiver(result);

									}
								});
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	public void closeServer() {
		if (ds != null) {
			ds.close();
			flag=false;
		}
	}

	public interface OnUdpListener {
		public void onMessageReceiver(String msg);
	}
}
