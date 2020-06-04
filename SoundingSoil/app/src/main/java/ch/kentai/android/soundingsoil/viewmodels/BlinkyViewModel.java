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

package ch.kentai.android.soundingsoil.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import androidx.annotation.NonNull;

import ch.kentai.android.soundingsoil.R;
import ch.kentai.android.soundingsoil.adapter.DiscoveredBluetoothDevice;
import ch.kentai.android.soundingsoil.profile.BlinkyManager;
import ch.kentai.android.soundingsoil.profile.BlinkyManagerCallbacks;
import ch.kentai.android.soundingsoil.scanner.SimpleBluetoothDevice;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyViewModel extends AndroidViewModel implements BlinkyManagerCallbacks {
	private final BlinkyManager mBlinkyManager;
	private BluetoothDevice mDevice;

	// Connection states Connecting, Connected, Disconnecting, Disconnected etc.
	private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

	// Flag to determine if the device is connected
	private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

	// Flag to determine if the device has required services
	private final MutableLiveData<Boolean> mIsSupported = new MutableLiveData<>();

	// Flag to determine if the device is ready
	private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

	// Flag that holds the on off monitor state. On is true, Off is False
	private final MutableLiveData<Boolean> mMonState = new MutableLiveData<>();

	// Flag that holds the record state. 0 = off, 1 = pause, 2 = recording 3 = preparing
	private final MutableLiveData<Integer> mRecState = new MutableLiveData<>();

	// current number of record in a record serie
	private final MutableLiveData<Integer> mRecNumber = new MutableLiveData<>();

	// Flag that holds the pressed released state of the button on the devkit.
	// Pressed is true, Released is false
	private final MutableLiveData<Boolean> mButtonState = new MutableLiveData<>();

	private final MutableLiveData<String> mDataReceived = new MutableLiveData<>();

	private final MutableLiveData<String> mNextRecTime = new MutableLiveData<>();

	private final MutableLiveData<String> mRecRem = new MutableLiveData<>();

	private final MutableLiveData<String> mTimeReq = new MutableLiveData<>();

	private final MutableLiveData<String> mLatlongReq = new MutableLiveData<>();

	private final MutableLiveData<String> mDataSent = new MutableLiveData<>();

	private final MutableLiveData<ArrayList<String>> mDataSentReceived = new MutableLiveData<>();

	private final MutableLiveData<String> mDuration = new MutableLiveData<>();
	private final MutableLiveData<String> mPeriod = new MutableLiveData<>();
    private final MutableLiveData<String> mOccurence = new MutableLiveData<>();

	private final MutableLiveData<String> mFilepath = new MutableLiveData<>();

	private final MutableLiveData<String> mVolume = new MutableLiveData<>();

	private final MutableLiveData<String> mLatitude = new MutableLiveData<>();

	private final MutableLiveData<String> mLongitude = new MutableLiveData<>();

	private final MutableLiveData<SimpleBluetoothDevice> mDeviceDiscovered = new MutableLiveData<>();

	private final MutableLiveData<String> mBTStateChanged = new MutableLiveData<>();

	private final MutableLiveData<Boolean> mInqState = new MutableLiveData<>();

	private final MutableLiveData<String> mRemainingTime = new MutableLiveData<>();

	private String latitude = "";
	private String longitude = "";

	public void setLatitude(String lat) {latitude = lat;}
	public void setLongitude(String _long) {longitude = _long;}


    public LiveData<Void> isDeviceReady() {
		return mOnDeviceReady;
	}

	public LiveData<String> getConnectionState() {
		return mConnectionState;
	}

	public LiveData<Boolean> isConnected() {
		return mIsConnected;
	}

	public LiveData<Boolean> getButtonState() {
		return mButtonState;
	}

	public LiveData<Boolean> getMonState() {
		return mMonState;
	}

	public LiveData<Integer> getRecState() {
		return mRecState;
	}

	public LiveData<Integer> getRecNumber() {
		return mRecNumber;
	}

	public LiveData<Boolean> isSupported() {
		return mIsSupported;
	}

	public LiveData<String> getDataReceived() {
		return mDataReceived;
	}

	public LiveData<String> getNextRecTime() {
		return mNextRecTime;
	}

	public  LiveData<String> getRecRem() {
		return  mRecRem;
	}

	public  LiveData<String> getTimeReq() {
		return mTimeReq;
	}

	public LiveData<String> getLatlongReq() {
		return mLatlongReq;
	}

	public LiveData<String> getDataSent() {
		return mDataSent;
	}

	public LiveData<ArrayList<String>> getDataSentReceived() {
		return mDataSentReceived;
	}

	//@Bindable
	public LiveData<String> getDuration() {return mDuration;}

	public LiveData<String> getPeriod() {return mPeriod;}

    public LiveData<String> getOccurence() {return  mOccurence;}

	public LiveData<String> getFilepath() {return  mFilepath;}

	public LiveData<String> getVolume() {return  mVolume;}

	public LiveData<String> getLatitude() {return  mLatitude;}

	public LiveData<String> getLongitude() {return  mLongitude;}

	public LiveData<SimpleBluetoothDevice> getDeviceDiscovered() {return  mDeviceDiscovered;}

	public LiveData<String> getBTStateChanged() {return  mBTStateChanged;}

	public LiveData<Boolean> getInqState() {
		return mInqState;
	}

	public LiveData<String> getRemainingTime() { return mRemainingTime; }

	private static final String TAG = "BlinkyViewModel";

	public BlinkyViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager
		mBlinkyManager = new BlinkyManager(getApplication());
		mBlinkyManager.setGattCallbacks(this);

		mDataSentReceived.setValue(new ArrayList<String>());
		mMonState.setValue(false);
		mDuration.setValue("300");
		mBTStateChanged.postValue("DISCONNECTED");
		mConnectionState.setValue("");
		mRecNumber.setValue(0);
		mRecRem.setValue("");
		mTimeReq.setValue("");
		mLatlongReq.setValue("");
		mIsConnected.setValue(false);
		//mNextRecTime.setValue("");
		mLatitude.setValue("");
		mLongitude.setValue("");
	}

	/**
	 * Connect to peripheral.
	 */
	public void connect(@NonNull final DiscoveredBluetoothDevice device) {
		// Prevent from calling again when called again (screen orientation changed)
		if (mDevice == null) {
			mDevice = device.getDevice();
			final LogSession logSession
					= Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
			mBlinkyManager.setLogger(logSession);
			reconnect();
		}
	}

	/**
	 * Reconnects to previously connected device.
	 * If this device was not supported, its services were cleared on disconnection, so
	 * reconnection may help.
	 */
	public void reconnect() {
		if (mDevice != null) {
			mBlinkyManager.connect(mDevice)
					.retry(3, 100)
					.useAutoConnect(false)
					.enqueue();
		}
	}

	/**
	 * Disconnect from peripheral.
	 */
	public void disconnect() {
		mDevice = null;
		mBlinkyManager.disconnect().enqueue();
		Log.w("tag", "disconnect!");

	}

	public void toggleMon() {
		if(mMonState.getValue()) {
			mBlinkyManager.send("mon stop");
			//mMonState.setValue(false);
		}
		else {
			mBlinkyManager.send("mon start");
			//mMonState.setValue(true);
		}

	}

	public void toggleRec() {
		if(mRecState.getValue() == null || mRecState.getValue() == 0) {
			long unixTime = System.currentTimeMillis() / 1000;
			unixTime += getCurrentTimezoneOffset();		// add time zone offset
			mBlinkyManager.send("time " + unixTime);
//			mBlinkyManager.send("latlong " + latitude + " " + longitude);
			mBlinkyManager.send("rwin " + mDuration.getValue() + " " +  mPeriod.getValue() + " " + mOccurence.getValue() );
			mBlinkyManager.send("rec start");
			mRecState.setValue(3);		// 3 == rec preparing
		}
		else {
			mBlinkyManager.send("rec stop");
		}

	}

	public void sendStringToBlinkyManager(final String string) {
		mBlinkyManager.send(string);
	}

	public void sendRwinParams() {
		mBlinkyManager.send("rwin " + mDuration.getValue() + " " +  mPeriod.getValue() + " " + mOccurence.getValue() );
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (mBlinkyManager.isConnected()) {
			disconnect();
		}
	}

    @Override
	public void onMonStateChanged(@NonNull final BluetoothDevice device, final boolean on) {
		mMonState.postValue(on);
	}

	@Override
	public void onRecStateChanged(@NonNull final BluetoothDevice device, final int state) {
		mRecState.postValue(state);
	}

	@Override
	public void onRecNumberChanged(@NonNull final BluetoothDevice device, final int recNumber) {
		mRecNumber.postValue(recNumber);
	}

	@Override
	public void onNextRecTimeChanged(@NonNull final BluetoothDevice device, final String recTime) {
		mNextRecTime.postValue(recTime);
	}

	@Override
	public void onRecRemChanged(@NonNull final BluetoothDevice device, final String recRem) {
		mRecRem.postValue(recRem);
	}

	@Override
	public void onTimeReqChanged(@NonNull final BluetoothDevice device, final String timeReq) {
		mTimeReq.postValue(timeReq);
	}

	@Override
	public void onLatlongReqChanged(@NonNull final BluetoothDevice device, final String latlongReq) {
		mLatlongReq.postValue(latlongReq);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, final String string) {
		mDataReceived.postValue(string);
		addToDataSentReceived("R: " + string);
	}

	@Override
	public void onDataSent(@NonNull final BluetoothDevice device, final String string) {
		mDataSent.postValue(string);
		addToDataSentReceived("S: " + string);
	}

	@Override
	public void onDurationChanged(@NonNull final BluetoothDevice device, final String string) {
		mDuration.postValue(string);
		Log.w("tag", "onDurChanged " + string);

	}

	@Override
	public void onPeriodChanged(@NonNull final BluetoothDevice device, final String string) {
		mPeriod.postValue(string);
		Log.w("tag", "onPeriodChanged " + string);

	}

	@Override
	public void onOccurenceChanged(@NonNull final BluetoothDevice device, final String string) {
		mOccurence.postValue(string);
		Log.w("tag", "onOccChanged " + string);

	}

	@Override
	public void onFilepathChanged(@NonNull final BluetoothDevice device, final String string) {
		mFilepath.postValue(string);
		Log.w("tag", "onFPChanged " + string);

	}

	@Override
	public void onVolumeChanged(@NonNull final BluetoothDevice device, final String string) {
		mVolume.postValue(string);
		Log.w("tag", "onVolChanged " + string);

	}

	@Override
	public void onLatitudeChanged(@NonNull final BluetoothDevice device, final String string) {
		mLatitude.postValue(string);
		Log.w("tag", "onLatitudeChanged " + string);
	}

	@Override
	public void onLongitudeChanged(@NonNull final BluetoothDevice device, final String string) {
		mLongitude.postValue(string);
		Log.w("tag", "onLongitudeChanged " + string);
	}

	@Override
	public void onDeviceDiscovered(@NonNull final SimpleBluetoothDevice device) {
		mDeviceDiscovered.postValue(device);
		Log.w("tag", "onDevicesDiscovered " + device.name + " " + device.rssi);

	}

	@Override
	public void onInqStateChanged(@NonNull boolean state) {
		mInqState.postValue(state);
		Log.w("tag", "onInqStateChanged " + state);
	}

	@Override
	public void onBTStateChanged(@NonNull final String string) {
		mBTStateChanged.postValue(string.toUpperCase());
		Log.w("tag", "onBTStateChanged " + string);

	}




	@Override
	public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
		mConnectionState.postValue(getApplication().getString(R.string.state_connecting));
	}

	@Override
	public void onDeviceConnected(@NonNull final BluetoothDevice device) {
		mIsConnected.postValue(true);
		mConnectionState.postValue(getApplication().getString(R.string.state_discovering_services));
	}

	@Override
	public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device,
									 final boolean optionalServicesFound) {
		mConnectionState.postValue(getApplication().getString(R.string.state_initializing));
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		mIsSupported.postValue(true);
		mConnectionState.postValue(null);
		mOnDeviceReady.postValue(null);
	}

	@Override
	public void onBondingRequired(@NonNull final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onBonded(@NonNull final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onBondingFailed(@NonNull final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onError(@NonNull final BluetoothDevice device,
						@NonNull final String message, final int errorCode) {
		// TODO implement
	}

	@Override
	public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
		mConnectionState.postValue(null);
		mIsSupported.postValue(false);
	}

	public void clearDataSentReceived(){
		ArrayList<String> clonedStr = new ArrayList<String>();
		mDataSentReceived.setValue(clonedStr);
	}

	private void addToDataSentReceived(String string) {
		ArrayList<String> str = mDataSentReceived.getValue();

		ArrayList<String> clonedStr = new ArrayList<String>(str.size() + 1);
		clonedStr.add(string);
		for(int i = 0; i < str.size(); i++){
			clonedStr.add(str.get(i));
		}

		mDataSentReceived.setValue(clonedStr);

	}

	public void requestDeviceStatus() {
		//
		Log.w("tag", "requestDeviceStatus");
		mBlinkyManager.send("rec ?");
		mBlinkyManager.send("mon ?");


		final Handler handler1 = new Handler();
		handler1.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBlinkyManager.send("rwin ?");
				mBlinkyManager.send("latlong ?");
			}
		}, 500);


		final Handler handler2 = new Handler();
		handler2.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBlinkyManager.send("filepath ?");
				mBlinkyManager.send("vol ?");
			}
		}, 1000);


		final Handler handler3 = new Handler();
		handler3.postDelayed(new Runnable() {
			@Override
			public void run() {
				// show monitor elements as inactive
				mBlinkyManager.send("rec_next ?");
				mBlinkyManager.send("rec_nb ?");
			}
		}, 1500);

		final Handler handler4 = new Handler();
		handler4.postDelayed(new Runnable() {
			@Override
			public void run() {
				// show monitor elements as inactive
				mBlinkyManager.send("bt ?");
			}
		}, 2000);



	}

	public void sendCurrentTime() {
		long now = System.currentTimeMillis() / 1000;
		now += getCurrentTimezoneOffset();		// add time zone offset
		Log.d(TAG, "Sending current timestamp: " + now);
		mBlinkyManager.send("time " + now);
	}

	public void sendLatLong() {
		Log.d(TAG, "Sending latlong");
		mBlinkyManager.send("latlong " + latitude + " " + longitude);
	}

	public void setDuration(String string	) {
		mDuration.setValue(string);
	}

	public void setPeriod(String period) {
		mPeriod.setValue(period);
	}
	public void setOccurence(String occurence) {
		mOccurence.setValue(occurence);
	}


	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.w("tag", "onTextChanged " + s);
	}


	public static int getCurrentTimezoneOffset() {

		TimeZone tz = TimeZone.getDefault();
		Calendar cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
		int offsetInSeconds = offsetInMillis / 1000;

		return offsetInSeconds;
	}


}
