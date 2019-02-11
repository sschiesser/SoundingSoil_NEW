/*
 used for scanning for a2dp devices
 */
package ch.kentai.android.soundingsoil.scanner;


public class SimpleBluetoothDevice {
	/* package */ static final int NO_RSSI = -1000;
	public String name;	// used for address, since we have only the address
	public String rssi;
	//public String address;


	public SimpleBluetoothDevice() {
		this.name = "";
		this.rssi = "";
	}

	public SimpleBluetoothDevice(final String name, String rssi) {
		this.name = name;
		this.rssi = rssi;
	}

	public boolean matches(final SimpleBluetoothDevice _device) {
		return name.equals(_device.name);
	}
}
