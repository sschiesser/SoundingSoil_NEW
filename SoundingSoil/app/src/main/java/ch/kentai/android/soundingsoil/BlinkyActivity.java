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

package ch.kentai.android.soundingsoil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
//import android.app.FragmentManager;
//import android.support.v4.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.FormatException;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import android.support.v4.content.ContextCompat;


import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;

import com.newventuresoftware.waveform.WaveformView;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import ch.kentai.android.soundingsoil.adapter.DiscoveredBluetoothDevice;
import ch.kentai.android.soundingsoil.databinding.ActivityBlinkyBinding;
import ch.kentai.android.soundingsoil.scanner.SimpleBluetoothDevice;
import ch.kentai.android.soundingsoil.viewmodels.BlinkyViewModel;
import ch.kentai.android.soundingsoil.scanner.ScannerFragment;
import ch.kentai.android.soundingsoil.utils.RepeatListener;
import ch.kentai.android.soundingsoil.HelpFragment;

import static ch.kentai.android.soundingsoil.viewmodels.BlinkyViewModel.getCurrentTimezoneOffset;

@SuppressWarnings("ConstantConditions")
public class BlinkyActivity extends AppCompatActivity implements ScannerFragment.OnDeviceSelectedListener, HelpFragment.OnFragmentInteractionListener {

	private WaveformView mRealtimeWaveformView;
	private RecordingThread mRecordingThread;


	public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

	private BlinkyViewModel mViewModel;


	@BindView(R.id.data_sent_received) TextView mDataReceived;

	@BindView(R.id.duration_text) EditText mDurationField;
	@BindView(R.id.period_text) EditText mPeriodField;
	@BindView(R.id.occurence_text) EditText mOccurenceField;

	@BindView(R.id.mon_button) Button mMonButton;
	@BindView(R.id.mon_state) TextView mMonState;

	@BindView(R.id.connex_state) TextView mConnexState;

	@BindView(R.id.rec_button) ImageButton mRecButton;
	@BindView(R.id.rec_state) TextView mRecStateView;

	@BindView(R.id.vol_up_button) ImageButton mVolUpButton;
	@BindView(R.id.vol_down_button) ImageButton mVolDownButton;

	@BindView(R.id.status_req_button) Button mStatusReqButton;
	@BindView(R.id.clear_mon_button) Button mClearButton;
	@BindView(R.id.close_mon_button) Button mCloseButton;

    @BindView(R.id.conn_button) Button mConnButton;
	@BindView(R.id.rec_settings_reset_button) Button mSettingsResetButton;
	@BindView(R.id.rec_settings_close_button) Button mSettingsCloseButton;


    @BindView(R.id.mon_button_part) LinearLayout mon_part;
    @BindView(R.id.vol_control_part) LinearLayout vol_part;

	@BindView(R.id.rec_time) TextView mRecTimeView;
	@BindView(R.id.rec_number) TextView mRecNumberView;
	@BindView(R.id.rec_number_part) LinearLayout mRecNumberPart;
	@BindView(R.id.file_path_part) LinearLayout mFilePathPart;

	@BindView(R.id.comm_log) LinearLayout commMonitor;
	@BindView(R.id.rec_settings) LinearLayout recSettings;

	@BindView(R.id.mon_state_led) ImageView monLED;
	@BindView(R.id.connex_state_led) ImageView connexLED;
	@BindView(R.id.help) View mHelp;

    private ObjectAnimator anim;
	private static final String TAG = "BlinkyActivity";

	private ProgressBar pg;

	private static final int REQUEST_CODE = 0;
	static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO};
	private  int recTime = 0;
	private  int nextRecTime = 0;
	private int recState = 0;

	private boolean recSettingsEditable = false;

	private String mLatitude;
	private String mLongitude;

	MenuItem connectItem;

	Drawable redConnectIcon;
	Drawable greenConnectIcon;




	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.activity_blinky);

		final Intent intent = getIntent();
		final DiscoveredBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
		final String deviceName = device.getName();
		final String deviceAddress = device.getAddress();

		// Configure the view model
		mViewModel = ViewModelProviders.of(this).get(BlinkyViewModel.class);
		mViewModel.connect(device);

		ActivityBlinkyBinding binding =
				DataBindingUtil.setContentView(this, R.layout.activity_blinky);
		binding.setLifecycleOwner(this);
		binding.setViewmodel(mViewModel);

		ButterKnife.bind(this);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName.toUpperCase());
		//getSupportActionBar().setSubtitle(deviceAddress);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Set up views
		final LinearLayout progressContainer = findViewById(R.id.progress_container);
		final TextView connectionState = findViewById(R.id.connection_state);
		final View content = findViewById(R.id.device_container);
		final View notSupported = findViewById(R.id.not_supported);


		final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
		animation.setDuration(500); // duration - half a second
		animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

		pg = findViewById(R.id.volumeBar);


		commMonitor.setVisibility(View.GONE);
		recSettings.setVisibility(View.GONE);

		mRealtimeWaveformView = (WaveformView) findViewById(R.id.waveformView);
		mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
			@Override
			public void onAudioDataReceived(short[] data) {
				mRealtimeWaveformView.setSamples(data);
			}
		});

        Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				//mRecordingSamplerReady = true;
			}
		}, 1000);


		Thread recTimer = new Thread() {
			@Override
			public void run() {

				while(!isInterrupted()) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (recState == 1) {
							// waiting: display next rec time
						} else if (recState == 2) {
							mRecTimeView.setText("REMAINING TIME: " + Integer.toString(recTime) + "s"); //this is the textview
						} else {
							// state stopped
							mRecTimeView.setText("--");
						}
					}
				});

				if (recState == 2 || recState == 1) {
					// state recording or waiting
					if (recTime > 0) {
						recTime -= 1;
					}
				} else {
					// state stopped or preparing
					recTime = 0;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
			}
		};
		recTimer.start();


		try {
			SQLiteDatabase mDataBase = this.openOrCreateDatabase("Presets", MODE_PRIVATE, null);
			mDataBase.execSQL("CREATE TABLE IF NOT EXISTS presets (duration VARCHAR, period VARCHAR, occurences VARCHAR)");
			mDataBase.execSQL("INSERT INTO presets (duration, period, ocurrences) VALUES ('10', '20', '3')");
		} catch (Exception e) {
			e.printStackTrace();
		}


		mDurationField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					mViewModel.setDuration(s.toString());
				} catch (NumberFormatException nfe) {
					Log.d(TAG, "period error");
				}
			}
		});

		mPeriodField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					mViewModel.setPeriod(s.toString());
				} catch (NumberFormatException nfe) {
					Log.d(TAG, "period error");
				}
			}
		});

		mOccurenceField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					mViewModel.setOccurence(s.toString());
				} catch (NumberFormatException nfe) {
					Log.d(TAG, "period error");
				}
			}
		});


		mMonButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.toggleMon();
			}
		});


		mRecButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			    // check permission
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    Location location = null;

                    if (isNetworkEnabled) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    } else if (isGPSEnabled) {
                        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }

                    if (location != null) {
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();

                        mLatitude = String.format("%.4f", latitude);
                        mLongitude = String.format("%.4f", longitude);

                        mViewModel.setLatitude(mLatitude);
                        mViewModel.setLongitude(mLongitude);
                    } else {
                    	// check if no latlong at all
						if (mLatitude == "" || mLongitude == "") {
							showAlertDialogNoLocation();
						}
					}
                }
				mViewModel.toggleRec();
			}
		});

		mStatusReqButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.requestDeviceStatus();
			}
		});



		mClearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.clearDataSentReceived();
			}
		});


        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commMonitor.setVisibility(View.GONE);
            }
        });

		mSettingsResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDurationField.setText("300");
				mPeriodField.setText("3600");
				mOccurenceField.setText("24");
				mViewModel.sendRwinParams();
			}
		});

		mSettingsCloseButton.setOnClickListener((v -> {

				recSettings.setVisibility(View.GONE);
				hideKeyboard(this);

		}));


		mVolUpButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.sendStringToBlinkyManager("vol +");
			}
		}
		));


		mVolDownButton.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.sendStringToBlinkyManager("vol -");
			}
		}
		));


		mConnButton.setOnClickListener((v -> {
			// check BT status. if connected -> disconnect
			String btState = mViewModel.getBTStateChanged().getValue();
			if(btState != null) {
				if(btState.equalsIgnoreCase("disconnected")) {
					mViewModel.sendStringToBlinkyManager("inq");
					showDeviceScanningDialog();
				} else {
					mViewModel.sendStringToBlinkyManager("disc");
				}
			}
		}));



		// observe -----------------------
		mViewModel.getBTStateChanged().observe(this, btState -> {
			Log.d(TAG, "Audio Monitor state: " + mViewModel.getMonState().getValue());

			if(btState.equalsIgnoreCase("disconnected")) {
				mRecordingThread.stopRecording();
				// turn off monitor if on
				if (mViewModel.getMonState().getValue()) {
					mViewModel.toggleMon();
				}
				connexLED.setColorFilter(Color.argb(255, 166, 51, 51));
				mConnexState.setText("--");

				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						vol_part.setAlpha(.5f);
						mVolDownButton.setEnabled(false);
						mVolUpButton.setEnabled(false);
						mConnButton.setText("CONNECT");
					}
				}, 500);

			} else { // connected
                mConnButton.setText("DISCONNECT");
				mConnexState.setText(btState.toUpperCase());
				connexLED.setColorFilter(Color.GREEN);

				dismissDeviceScanningDialog();

				if (mViewModel.getMonState().getValue()) {	// monitor on
					vol_part.setAlpha(1.0f);
					mVolDownButton.setEnabled(true);
					mVolUpButton.setEnabled(true);
					mRecordingThread.startRecording();
				}
            }
			Log.d(TAG, "Audio BT state: " + btState);
		});

		mViewModel.isDeviceReady().observe(this, deviceReady -> {
			progressContainer.setVisibility(View.GONE);
			//content.setVisibility(View.VISIBLE);
			mViewModel.requestDeviceStatus();
			Log.d(TAG, "device ready: " + deviceReady);
		});

		mViewModel.getConnectionState().observe(this, text -> {
			if (text != null) {
				progressContainer.setVisibility(View.VISIBLE);
				notSupported.setVisibility(View.GONE);
				connectionState.setText(text);
				Log.d(TAG, "connection state: " + text);
			}
		});

		mViewModel.isConnected().observe(this, this::onConnectionStateChanged);

		mViewModel.isSupported().observe(this, supported -> {
			if (!supported) {
				progressContainer.setVisibility(View.GONE);
				notSupported.setVisibility(View.VISIBLE);
			}
		});


		mViewModel.getMonState().observe(this, isOn -> {
			//mMonState.setText(isOn ? R.string.mon_state_on : R.string.mon_state_off);
				if (isOn) 	{
					monLED.setColorFilter(Color.GREEN);
					mMonButton.setText(R.string.mon_state_off);

					//mRecordingThread.startRecording();
					if (mViewModel.getBTStateChanged().getValue().equalsIgnoreCase("disconnected")) {
					} else {
						mRecordingThread.startRecording();
						vol_part.setAlpha(1.0f);
						mVolDownButton.setEnabled(true);
						mVolUpButton.setEnabled(true);

					}
				}
				else {
					monLED.setColorFilter(Color.argb(255, 166, 51, 51));

					mMonButton.setText(R.string.mon_state_on);
					mRecordingThread.stopRecording();
					vol_part.setAlpha(.5f);
					mVolDownButton.setEnabled(false);
					mVolUpButton.setEnabled(false);

				}
		});


		mViewModel.getRecState().observe(this, state -> {
			recState = state;
			if (state == 0) {
				mRecStateView.setText(R.string.rec_state_off);
				this.manageBlinkEffect(false);
//				mRecButton.setColorFilter(Color.argb(55, 0, 0, 0));
				mRecButton.setColorFilter(Color.argb(255, 166, 51, 51));
				//mRecStateView.setBackgroundColor(Color.WHITE);
				mRecNumberPart.setVisibility(View.GONE);
				mFilePathPart.setVisibility(View.GONE);
				mRecTimeView.setVisibility(View.GONE);
			}
			else if (state == 1) {
				mRecStateView.setText(R.string.rec_state_wait);
				this.manageBlinkEffect(true);
				// request time of next record to set wait countdown timer
				mRecNumberPart.setVisibility(View.GONE);
				mFilePathPart.setVisibility(View.GONE);
			}
			else if(state == 2) {
				mRecStateView.setText(R.string.rec_state_on);
				this.manageBlinkEffect(false);
				//mRecStateView.setBackgroundColor(Color.RED);
				mRecButton.setColorFilter(Color.argb(255, 250, 69, 32));
				mRecNumberPart.setVisibility(View.VISIBLE);
				mFilePathPart.setVisibility(View.VISIBLE);
				mRecTimeView.setVisibility(View.VISIBLE);
			}
			else if(state == 3) {
				mRecStateView.setText(R.string.rec_state_preparing);
				this.manageBlinkEffect(true);
				//mRecStateView.setBackgroundColor(Color.RED);
			}
		});


		mViewModel.getRecNumber().observe(this, recNumber -> {
			Log.d(TAG, "Rec Number: " + recNumber);
			mRecNumberView.setText(" " + recNumber + " of " + mViewModel.getOccurence().getValue());
		});


		mViewModel.getNextRecTime().observe(this, nextRectimeString -> {
			Log.d(TAG, "Next Rec Time: " + nextRectimeString);
			int nRecTime = Integer.parseInt(nextRectimeString);
			long now = System.currentTimeMillis() / 1000;
			now += getCurrentTimezoneOffset();		// add time zone offset
			nextRecTime = nRecTime - (int)now;

			// convert seconds to milliseconds
			Date date = new java.util.Date(nRecTime*1000L);
			// the format of your date
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
			// give a timezone reference for formatting (see comment at the bottom)
			sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
			String formattedDate = sdf.format(date);
			mRecTimeView.setText("NEXT RECORD AT: " + formattedDate);
			Log.d(TAG, "Next Rec in: " + nextRecTime + "s / " + formattedDate);
		});


		mViewModel.getFilepath().observe(this, path -> {
			if (path.equalsIgnoreCase("--")) {

			} else {
				// compare timestamp e.g. /190507/175838.wav from filepath with current time and duration setting to set rec time countdown timer
				int start = path.indexOf("/") + 1;
				int end = path.indexOf(".wav");
				if (start  < path.length() && end < path.length()) {
					String inputTime = path.substring(start, end);
					Log.d(TAG, "Rec Start time: " + inputTime);

					SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMdd/HHmmss");
					Date recStartDate = null;

					try {
						recStartDate = inputFormat.parse(inputTime);
					} catch (ParseException e) {
					}

					// get local time
					long now = System.currentTimeMillis();
					long diff = now - recStartDate.getTime();
					Log.d(TAG, "Rec time difference: " + now + " - " + recStartDate.getTime() + " = "  + diff);

					int dur;
					try {
						dur = Integer.parseInt(mViewModel.getDuration().getValue());
						Log.d(TAG, "Rec Duration: " + dur);
					}
					catch (NumberFormatException e)
					{
						dur = 0;
					}

					if (dur != 0) {
						// not endless recording duration
						// reset the count down
						recTime = dur - (int)(diff / 1000);
						Log.d(TAG, "remaining rec time: " + recTime);
					} else {
						recTime = 0;
					}
				}
			}
			Log.d(TAG, "Filepath: " + path);
		});


		mViewModel.getVolume().observe(this, string -> {
			try {
				int vol = (int)Float.parseFloat(string);
				pg.setProgress(vol);
				Log.d(TAG, "BT Vol: " + vol);
			} catch (NumberFormatException e) {

			}
		});


		mViewModel.getLatitude().observe(this, string
				-> {
			mLatitude = string;
		});

		mViewModel.getLongitude().observe(this, string
				-> {
			mLongitude = string;
		});

		mViewModel.getDataReceived().observe(this, string
				-> {
		});

		mViewModel.getDataSent().observe(this, string
				-> {
		});

		mViewModel.getDataSentReceived().observe(this, string
				-> {
			mDataReceived.setText("");
			for (String str : string) {
				mDataReceived.append(str + "\n");
			}
		});

		anim = ObjectAnimator.ofInt(mRecButton, "colorFilter", Color.argb(255, 166, 51, 51), Color.argb(255, 250, 69, 32));

	}


	@Override
	public void onDeviceSelected(final SimpleBluetoothDevice device) {
		mViewModel.sendStringToBlinkyManager("conn " + device.name);
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}



	@OnClick(R.id.action_clear_cache)
	public void onTryAgainClicked() {
		mViewModel.reconnect();
	}

	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = viewGroup.getChildAt(i);
			view.setEnabled(enabled);
			if (view instanceof ViewGroup) {
				enableDisableViewGroup((ViewGroup) view, enabled);
			}
		}
	}



	private void onConnectionStateChanged(final boolean connected) {
		Log.d(TAG, "connex state changed: " + connected);
		final View content = findViewById(R.id.device_container);
		if (connected) {
			mViewModel.requestDeviceStatus();
			content.setAlpha(1);
			enableDisableViewGroup((ViewGroup) content, true);

			if(greenConnectIcon != null) {
				connectItem.setIcon(greenConnectIcon);
			}

		}
		else {
			content.setAlpha(0.4f);
			enableDisableViewGroup((ViewGroup) content, false);

			if(redConnectIcon != null) {
				connectItem.setIcon(redConnectIcon);
			}
		}
	}

	private void manageBlinkEffect(boolean isOn) {
		if (isOn) {
			anim.setDuration(1000);
			anim.setEvaluator(new ArgbEvaluator());
			anim.setRepeatMode(ValueAnimator.REVERSE);
			anim.setRepeatCount(ValueAnimator.INFINITE);
			anim.start();
		} else {
			anim.end();
		}
	}

	private void showDeviceScanningDialog() {
		final ScannerFragment dialog = ScannerFragment.getInstance(); // Device that is advertising directly does not have the GENERAL_DISCOVERABLE nor LIMITED_DISCOVERABLE flag set.
		dialog.show(getSupportFragmentManager(), "scan_fragment");
	}

	private void dismissDeviceScanningDialog() {

		Fragment prev = getSupportFragmentManager().findFragmentByTag("scan_fragment");
		if (prev != null) {
			ScannerFragment df = (ScannerFragment) prev;
			df.dismiss();
		}
	}



	@Override
	protected void onStop() {
		super.onStop();

		//mRecordingThread.stopRecording();
	}


	@Override
	protected void onPause() {
		super.onPause();
		mRecordingThread.stopRecording();
	}

	@Override
	protected void onDestroy() {
		mViewModel.disconnect();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mViewModel.getMonState().getValue() && !mViewModel.getBTStateChanged().getValue().equalsIgnoreCase("disconnected")) {
			mRecordingThread.startRecording();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);

		connectItem = menu.findItem(R.id.status);

		greenConnectIcon = connectItem.getIcon();
		greenConnectIcon.mutate().setColorFilter(Color.argb(255, 0, 255, 0), PorterDuff.Mode.SRC_IN);

		redConnectIcon =
				getApplicationContext().getResources().getDrawable(R.drawable.icons8_disconnected_48, getApplicationContext().getTheme());

		redConnectIcon.mutate().setColorFilter(Color.argb(255, 255, 0, 0), PorterDuff.Mode.SRC_IN);


		if(mViewModel.isConnected().getValue()) {
			connectItem.setIcon(greenConnectIcon);
		} else {
			connectItem.setIcon(redConnectIcon);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case R.id.settings:
				if(recSettings.getVisibility() == View.GONE) {
					showAlertDialogChangeRecordSettings();
				} else {
					recSettings.setVisibility(View.GONE);
                    hideKeyboard(this);
				}

				return true;
			case  R.id.help:
				commMonitor.setVisibility(View.GONE);
				recSettings.setVisibility(View.GONE);
				hideKeyboard(this);
				mHelp.setVisibility(View.VISIBLE);
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.help, new HelpFragment(), "help_fragment");
				transaction.addToBackStack("help_fragment");
				transaction.commit();

				return true;
			case  R.id.monitor:
				if(commMonitor.getVisibility() == View.GONE) {
					closeHelpFragment();
					commMonitor.setVisibility(View.VISIBLE);
					recSettings.setVisibility(View.GONE);
					hideKeyboard(this);
				} else {
					commMonitor.setVisibility(View.GONE);
				}
				return true;
			default:
				return false;
		}
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
		}
	}


	public void showAlertDialogChangeRecordSettings() {
		// setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("CHANGE RECORD SETTINGS");
		builder.setMessage("Do you really want to change the record settings?\n\nThe default values are 500 / 3600 / 24");
		// add the buttons
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				closeHelpFragment();
				recSettingsEditable = true;
				recSettings.setVisibility(View.VISIBLE);
				commMonitor.setVisibility(View.GONE);
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				recSettingsEditable = false;
			}
		});
		// create and show the alert dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void showAlertDialogNoLocation() {
		// setup the alert builder
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("NO LOCATION DETERMINED");
		builder.setMessage("The location couldn't be determined\n\nPlease note the location by yourself");
		// add the buttons
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		// create and show the alert dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	public void onFragmentInteraction(int count) {

	}


	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
			mHelp.setVisibility(View.GONE);
			//getSupportFragmentManager().findFragmentByTag("help_fragment");

		} else {
			super.onBackPressed();
		}
	}

	private void closeHelpFragment() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
			mHelp.setVisibility(View.GONE);
		}
	}


}
