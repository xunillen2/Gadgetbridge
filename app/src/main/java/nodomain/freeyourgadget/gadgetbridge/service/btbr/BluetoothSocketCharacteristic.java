/*  Copyright (C) 2015-2021 Andreas Shimokawa, Carsten Pfeiffer, Uwe Hermann
 *
 *   This file is part of Gadgetbridge.
 *
 *   Gadgetbridge is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Gadgetbridge is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nodomain.freeyourgadget.gadgetbridge.service.btbr;

import java.util.UUID;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

/**
 * Represents a Bleutooth Socket data
 *
 * <p>A socket data is a basic data element used to construct a Socket service.
 * The data contains a value only.
 * It is the counterpart of BluetoothGattCharacteristic.
*/

public class BluetoothSocketCharacteristic implements Parcelable {

  /**
   * The cached value of this characteristic.
   * @hide
  */
  private byte[] mValue;

  /**
   * The UUID of this characteristic.
   * @hide
  */
  protected UUID mUuid;

  public BluetoothSocketCharacteristic(byte[] value, UUID uuid) {
    mValue = value;
    mUuid = uuid;
  }

  /**
   * Returns the value of this characteristics
   *
   * @return value of this characteristic
   */
  public byte[] getValue() {
    return mValue;
  }

  public boolean setValue(byte[] value) {
        mValue = value;
        return true;
    }

  /**
   * Returns the UUID of this characteristic
   *
   * @return UUID of this characteristic
   */
  public UUID getUuid() {
    return mUuid;
  }

  public boolean setUuid(UUID uuid) {
    mUuid = uuid;
    return true;
  }

  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeByteArray(mValue);
    parcel.writeParcelable(new ParcelUuid(mUuid), 0);
  }

  protected BluetoothSocketCharacteristic(Parcel in) {
    mValue = in.createByteArray();
    mUuid = ((ParcelUuid)in.readParcelable(null)).getUuid();
  }

  public static final Creator<BluetoothSocketCharacteristic> CREATOR = new Creator<BluetoothSocketCharacteristic>() {
    @Override
    public BluetoothSocketCharacteristic createFromParcel(Parcel in) {
        return new BluetoothSocketCharacteristic(in);
    }

    @Override
    public BluetoothSocketCharacteristic[] newArray(int size) {
        return new BluetoothSocketCharacteristic[size];
    }
  };
}
