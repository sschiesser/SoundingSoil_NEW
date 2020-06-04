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

import ch.kentai.android.soundingsoil.scanner.SimpleBluetoothDevice;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyManagerCallbacks extends BleManagerCallbacks {

//    void onLedStateChanged(@NonNull final BluetoothDevice device, final boolean on);

    void onMonStateChanged(@NonNull final BluetoothDevice device, final boolean on);

    void onRecStateChanged(@NonNull final BluetoothDevice device, final int state);

    void onRecNumberChanged(@NonNull final BluetoothDevice device, final int recNumber);

    void onNextRecTimeChanged(@NonNull final BluetoothDevice device, final String string);

    void onRecRemChanged(@NonNull final BluetoothDevice device, final String recRec);

    void onTimeReqChanged(@NonNull final BluetoothDevice device, final String timeReq);

    void onDataReceived(@NonNull final BluetoothDevice device, final String string);

    void onDataSent(@NonNull final BluetoothDevice device, final String string);

    void onDurationChanged(@NonNull final BluetoothDevice device, final String string);

    void onPeriodChanged(@NonNull final BluetoothDevice device, final String string);

    void onOccurenceChanged(@NonNull final BluetoothDevice device, final String string);

    void onFilepathChanged(@NonNull final BluetoothDevice device, final String string);

    void onVolumeChanged(@NonNull final BluetoothDevice device, final String string);

    void onLongitudeChanged(@NonNull final BluetoothDevice device, final String string);

    void onLatitudeChanged(@NonNull final BluetoothDevice device, final String string);

    void onDeviceDiscovered(@NonNull final SimpleBluetoothDevice device);

    void onInqStateChanged(@NonNull final boolean state);

    void onBTStateChanged(@NonNull final String string);

}
