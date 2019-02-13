/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.kentai.android.soundingsoil.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Scanner;
import java.util.UUID;

import ch.kentai.android.soundingsoil.scanner.SimpleBluetoothDevice;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.WriteRequest;
import no.nordicsemi.android.ble.data.Data;
import ch.kentai.android.soundingsoil.profile.callback.BlinkyDataCallback;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
	/** Nordic Blinky Service UUID. */
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("bc2f4cc6-aaef-4351-9034-d66268e328f0");    // ssoil custom service
//    public final static UUID LBS_UUID_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	/** BUTTON characteristic UUID. */
	private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("06d1e5e7-79ad-4a71-8faa-373789f7d93c"); // test for receiving value changes
	/** LED characteristic UUID. */
	private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("06d1e5e7-79ad-4a71-8faa-373789f7d93c"); // ssoil custom characteristic

	// logilink receiver: BT0022 		0019070CB798

	private BluetoothGattCharacteristic mButtonCharacteristic, mCustomCharacteristic;
	private LogSession mLogSession;
	private boolean mSupported;
	private boolean mLedOn;
    private static final String TAG = "BlinkyManager";

	public BlinkyManager(@NonNull final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * Sets the log session to be used for low level logging.
	 * @param session the session, or null, if nRF Logger is not installed.
	 */
	public void setLogger(@Nullable final LogSession session) {
		this.mLogSession = session;
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
		// The priority is a Log.X constant, while the Logger accepts it's log levels.
		Logger.log(mLogSession, LogContract.Log.Level.fromPriority(priority), message);
	}

	@Override
	protected boolean shouldClearCacheWhenDisconnected() {
		return !mSupported;
	}


	/**
	 * The Data callback will be notified when the custom characteristic state was read or sent to the target device.
	 * <p>
	 * This callback implements both {@link no.nordicsemi.android.ble.callback.DataReceivedCallback}
	 * and {@link no.nordicsemi.android.ble.callback.DataSentCallback} and calls the same
	 * method on success.
	 * <p>
	 */
	private final BlinkyDataCallback mDataCallback = new BlinkyDataCallback() {
		@Override
		public void onDataSent(@NonNull final BluetoothDevice device,
									  final Data data) {

			String str = data.getStringValue(0);
			Log.d(TAG, "onDataSent: " + str);

			mCallbacks.onDataSent(device, str);
		}

		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device,
										  @NonNull final Data data) {

			String str = data.getStringValue(0);
            Log.d(TAG, "onDataReceived: " + str);

            mCallbacks.onDataReceived(device, str);

            if(str.equalsIgnoreCase("mon off")) {
                mCallbacks.onMonStateChanged(device, false);
            } else if (str.equalsIgnoreCase("mon on")) {
                mCallbacks.onMonStateChanged(device, true);
            } else if (str.equalsIgnoreCase("rec on")) {
				mCallbacks.onRec2StateChanged(device, 2);
			} else if (str.equalsIgnoreCase("rec off")) {
				mCallbacks.onRec2StateChanged(device, 0);
			} else if (str.equalsIgnoreCase("rec wait")) {
				mCallbacks.onRec2StateChanged(device, 1);
			} else if (str.startsWith("RWIN")) {
				Scanner scanner = new Scanner(str);
				scanner.next();
				mCallbacks.onDurationChanged(device, scanner.next());
				mCallbacks.onPeriodChanged(device, scanner.next());
				mCallbacks.onOccurenceChanged(device, scanner.next());
			} else if (str.startsWith("FP")) {
				mCallbacks.onFilepathChanged(device, str);
			} else if (str.startsWith("BT")) {
				Scanner scanner = new Scanner(str);
				scanner.next();
				mCallbacks.onBTStateChanged(scanner.next());
			} else if (str.startsWith("VOL") || str.startsWith("ERR")) {
				mCallbacks.onVolumeChanged(device, str);
			} else if (str.startsWith("INQ")) {
            	if (str.equalsIgnoreCase("INQ START")) {
					mCallbacks.onInqStateChanged(true);

				} else if (str.equalsIgnoreCase("INQ DONE")) {
					mCallbacks.onInqStateChanged(false);
				} else {
				Scanner scanner = new Scanner(str);
				scanner.next();
				SimpleBluetoothDevice dev = new SimpleBluetoothDevice();
				dev.name = scanner.next();		// test if scanner has next. test if number
				dev.rssi = scanner.next();
				mCallbacks.onDeviceDiscovered(dev);
            	}
            }
		}

	};

	/**
	 * BluetoothGatt callbacks object.
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
		@Override
		protected void initialize() {
            setNotificationCallback(mCustomCharacteristic).with(mDataCallback);
			readCharacteristic(mCustomCharacteristic).with(mDataCallback).enqueue();
            enableNotifications(mCustomCharacteristic).enqueue();
			requestMtu(43).enqueue();
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
			if (service != null) {
				mCustomCharacteristic = service.getCharacteristic(LBS_UUID_LED_CHAR);
			}

			boolean writeRequest = false;
			if (mCustomCharacteristic != null) {
				final int rxProperties = mCustomCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			}

            mSupported = mCustomCharacteristic != null && writeRequest;
			return mSupported;
		}

		@Override
		protected void onDeviceDisconnected() {
			mCustomCharacteristic = null;
		}
	};


	/**
	 * Sends the given text to RX characteristic.
	 * @param text the text to be sent
	 */
	public void send(final String text) {
		// Are we connected?
		if (mCustomCharacteristic == null)
			return;

		if (!TextUtils.isEmpty(text)) {
			final WriteRequest request = writeCharacteristic(mCustomCharacteristic, text.getBytes())
					.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
							"\"" + data.getStringValue(0) + "\" sent"));
          	//Log.d(TAG, "Sending String " + text + "...");
			request.with(mDataCallback).enqueue();
		}
	}

}
