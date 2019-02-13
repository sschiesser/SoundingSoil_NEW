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
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

	// Flag that holds the record state. 0 = off, 1 = pause, 2 = recording
	private final MutableLiveData<Integer> mRec2State = new MutableLiveData<>();

	// Flag that holds the pressed released state of the button on the devkit.
	// Pressed is true, Released is false
	private final MutableLiveData<Boolean> mButtonState = new MutableLiveData<>();

	private final MutableLiveData<String> mDataReceived = new MutableLiveData<>();

	private final MutableLiveData<String> mDataSent = new MutableLiveData<>();

	private final MutableLiveData<ArrayList<String>> mDataSentReceived = new MutableLiveData<>();

	private final MutableLiveData<String> duration = new MutableLiveData<>();
	private final MutableLiveData<String> mPeriod = new MutableLiveData<>();
    private final MutableLiveData<String> mOccurence = new MutableLiveData<>();

	private final MutableLiveData<String> mFilepath = new MutableLiveData<>();

	private final MutableLiveData<String> mVolume = new MutableLiveData<>();

	private final MutableLiveData<SimpleBluetoothDevice> mDeviceDiscovered = new MutableLiveData<>();

	private final MutableLiveData<String> mBTStateChanged = new MutableLiveData<>();

	private final MutableLiveData<Boolean> mInqState = new MutableLiveData<>();


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

	public LiveData<Integer> getRec2State() {
		return mRec2State;
	}

	public LiveData<Boolean> isSupported() {
		return mIsSupported;
	}

	public LiveData<String> getDataReceived() {
		return mDataReceived;
	}

	public LiveData<String> getDataSent() {
		return mDataSent;
	}

	public LiveData<ArrayList<String>> getDataSentReceived() {
		return mDataSentReceived;
	}

	//@Bindable
	public LiveData<String> getDuration() {return duration;}

	public LiveData<String> getPeriod() {return mPeriod;}

    public LiveData<String> getOccurence() {return  mOccurence;}

	public LiveData<String> getFilepath() {return  mFilepath;}

	public LiveData<String> getVolume() {return  mVolume;}

	public LiveData<SimpleBluetoothDevice> getDeviceDiscovered() {return  mDeviceDiscovered;}

	public LiveData<String> getBTStateChanged() {return  mBTStateChanged;}

	public LiveData<Boolean> getInqState() {
		return mInqState;
	}




	private static final String TAG = "BlinkyViewModel";




	public BlinkyViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager
		mBlinkyManager = new BlinkyManager(getApplication());
		mBlinkyManager.setGattCallbacks(this);

		mDataSentReceived.setValue(new ArrayList<String>());
		mMonState.setValue(false);
		duration.setValue("11");
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
	private void disconnect() {
		mDevice = null;
		mBlinkyManager.disconnect().enqueue();
	}

//	public void toggleLED(final boolean isOn) {
//		if(isOn) mBlinkyManager.send("mon start");
//		else mBlinkyManager.send("mon stop");
//		mLEDState.setValue(isOn);
//	}

	public void toggleMon() {
		if(mMonState.getValue()) {
			mBlinkyManager.send("mon stop");
			mMonState.setValue(false);
		}
		else {
			mBlinkyManager.send("mon start");
			mMonState.setValue(true);
		}

	}

//	public void toggleRec(final boolean isOn) {
//		if(isOn) mBlinkyManager.send("rec start");
//		else mBlinkyManager.send("rec stop");

//		mRecState.setValue(isOn);
//	}

	public void toggleRec2() {
//		if (mRec2State != null) Log.d(TAG, "rec state " + mRec2State.getValue());
		if(mRec2State.getValue() == null || mRec2State.getValue() == 0) {
			//long unixTimestamp = Instant.now().getEpochSecond();
			long unixTime = System.currentTimeMillis() / 1000;
			mBlinkyManager.send("time " + unixTime);
			mBlinkyManager.send("rwin " + duration.getValue() + " " +  mPeriod.getValue() + " " + mOccurence.getValue() );
			mBlinkyManager.send("rec start");
			//mRec2State.setValue(2);	// setvalue 3? == rec preparing
			Log.i("time", getDateCurrentTimeZone(unixTime));
		}
		else {
			mBlinkyManager.send("rec stop");
			//mRec2State.setValue(0);
		}

	}

	public void sendStringToBlinkyManager(final String string) {
		mBlinkyManager.send(string);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (mBlinkyManager.isConnected()) {
			disconnect();
		}
	}


//	public void onButtonStateChanged(@NonNull final BluetoothDevice device, final boolean pressed) {
//		mButtonState.postValue(pressed);
//	}

//    @Override
//    public void onLedStateChanged(@NonNull final BluetoothDevice device, final boolean on) {
//        mLEDState.postValue(on);
//    }

    @Override
	public void onMonStateChanged(@NonNull final BluetoothDevice device, final boolean on) {
		mMonState.postValue(on);
	}

	@Override
	public void onRec2StateChanged(@NonNull final BluetoothDevice device, final int state) {
		mRec2State.postValue(state);
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
		duration.postValue(string);
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
		mBTStateChanged.postValue(string);
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
        mBlinkyManager.send("bt ?");
        mBlinkyManager.send("vol ?");
        mBlinkyManager.send("rwin ?");
        mBlinkyManager.send("rec ?");
        mBlinkyManager.send("mon ?");
		mBlinkyManager.send("filepath ?");
    }

	public void setDuration(String string	) {
		duration.setValue(string);
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

	public  String getDateCurrentTimeZone(long timestamp) {
		try{
			Calendar calendar = Calendar.getInstance();
			TimeZone tz = TimeZone.getDefault();
			calendar.setTimeInMillis(timestamp * 1000);
			calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currenTimeZone = (Date) calendar.getTime();
			return sdf.format(currenTimeZone);
		}catch (Exception e) {
		}
		return "";
	}

}
