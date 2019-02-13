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

import ak.sh.ay.musicwave.MusicWave;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;

import com.tyorikan.voicerecordingvisualizer.RecordingSampler;
import com.tyorikan.voicerecordingvisualizer.VisualizerView;

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

@SuppressWarnings("ConstantConditions")
public class BlinkyActivity extends AppCompatActivity implements ScannerFragment.OnDeviceSelectedListener, RecordingSampler.CalculateVolumeListener {
	public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

	private BlinkyViewModel mViewModel;


	@BindView(R.id.data_sent_received) TextView mDataReceived;

	@BindView(R.id.duration_text) EditText mDurationField;
	@BindView(R.id.period_text) EditText mPeriodField;
	@BindView(R.id.occurence_text) EditText mOccurenceField;

	@BindView(R.id.mon_button) ImageButton mMonButton;
	@BindView(R.id.mon_state) TextView mMonState;

	@BindView(R.id.connex_state) TextView mConnexState;

	@BindView(R.id.rec_button) ImageButton mRecButton;
	@BindView(R.id.rec_state) TextView mRecState;

	@BindView(R.id.vol_up_button) ImageButton mVolUpButton;
	@BindView(R.id.vol_down_button) ImageButton mVolDownButton;

	@BindView(R.id.status_req_button) Button mStatusReqButton;
	@BindView(R.id.clear_mon_button) Button mClearButton;

	@BindView(R.id.conn_button) Button mConnButton;

	@BindView(R.id.connectedIV) ImageView mConnectedView;


	private ObjectAnimator anim;
	private static final String TAG = "BlinkyActivity";
	private boolean mRecordingSamplerReady = false;

	private ProgressBar pg;

//	private MediaRecorder mMediaRecorder;
//	private WaveformView waveformView;
//	private Visualizer mVisualizer;
//	private MusicWave musicWave;
//	private String outputFile;

	private RecordingSampler mRecordingSampler;
	private static final int REQUEST_CODE = 0;
	static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO};
//	static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS};



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
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setSubtitle(deviceAddress);
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

		pg = findViewById(R.id.pb);


//		ActivityCompat.requestPermissions(this,
//				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//				1);

		VisualizerView visualizerView = (VisualizerView) findViewById(R.id.visualizer);

		mRecordingSampler = new RecordingSampler();
		mRecordingSampler.setVolumeListener(this);  // for custom implements
		mRecordingSampler.setSamplingInterval(100); // voice sampling interval
		mRecordingSampler.link(visualizerView);     // link to visualizer
		mRecordingSampler.startRecording();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mRecordingSamplerReady = true;
			}
		}, 1000);

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


//        mConnectedView.setOnClickListener(v -> {
//            if (!mViewModel.isConnected().getValue()) mViewModel.reconnect();
//        });

		mMonButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.toggleMon();
			}
		});


		mRecButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mViewModel.toggleRec2();
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


//		mVolUpButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mViewModel.sendStringToBlinkyManager("vol +");
//			}
//		});

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
			mViewModel.sendStringToBlinkyManager("inq");
			showDeviceScanningDialog();
		}));

		mViewModel.isDeviceReady().observe(this, deviceReady -> {
			progressContainer.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			mViewModel.requestDeviceStatus();
			Log.d(TAG, "device ready" + deviceReady);
		});

		mViewModel.getConnectionState().observe(this, text -> {
			if (text != null) {
				progressContainer.setVisibility(View.VISIBLE);
				notSupported.setVisibility(View.GONE);
				connectionState.setText(text);
				Log.d(TAG, "connection state" + text);
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
			mMonState.setText(isOn ? R.string.mon_state_on : R.string.mon_state_off);
			//if (mRecordingSamplerReady) {
				if (isOn) 	mRecordingSampler.startRecording();
				else 		mRecordingSampler.stopRecording();
				//Log.i("recorder", "ready");
			//}
		});


		mViewModel.getRec2State().observe(this, state -> {
			if (state == 0) {
				mRecState.setText(R.string.rec_state_off);
				this.manageBlinkEffect(false);
				mRecState.setBackgroundColor(Color.WHITE);
			}
			else if (state == 1) {
				mRecState.setText(R.string.rec_state_wait);
				this.manageBlinkEffect(true);
			}
			else {
				mRecState.setText(R.string.rec_state_on);
				this.manageBlinkEffect(false);
				mRecState.setBackgroundColor(Color.RED);
			}
		});


		mViewModel.getVolume().observe(this, string -> {
			try {
				pg.setProgress((int)(Float.parseFloat(string) * 94.34));
			} catch (NumberFormatException e) {

			}
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

		anim = ObjectAnimator.ofInt(mRecState, "backgroundColor", Color.WHITE, Color.RED,
				Color.WHITE);

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

	private void onConnectionStateChanged(final boolean connected) {
		Log.d(TAG, "connex state changed: " + connected);
		if (connected) {
			mViewModel.requestDeviceStatus();
			//mConnexState.setText("Connected");
			mConnectedView.setImageResource(R.drawable.icons8_connected_48);
			mConnectedView.setColorFilter(Color.GREEN);
		}
		else {
			//mConnexState.setText("Disconnected");
			mConnectedView.setImageResource(R.drawable.icons8_disconnected_48);
			mConnectedView.setColorFilter(Color.RED);
		}
	}

	private void manageBlinkEffect(boolean isOn) {
		if (isOn) {
			anim.setDuration(1500);
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




//	private void startPermissionsActivity() {
//		PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
//	}
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
//			finish();
//		}
//	}

	@Override
	protected void onPause() {
		mRecordingSampler.stopRecording();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mRecordingSampler.release();
		super.onDestroy();
	}

	@Override
	public void onCalculateVolume(int volume) {
		// for custom implement
		//Log.d(TAG, String.valueOf(volume));
	}

}
