/*  Copyright (C) 2022 Damien Gaignon
*
*    This file is part of Gadgetbridge.
*
*    Gadgetbridge is free software: you can redistribute it and/or modify
*    it under the terms of the GNU Affero General Public License as published
*    by the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Gadgetbridge is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package nodomain.freeyourgadget.gadgetbridge.service.btbr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public final class BtBRQueue {
    private static final Logger LOG = LoggerFactory.getLogger(BtBRQueue.class);

    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket = null;
    //private InputStream mInStream = null;
    //private OutputStream mOutStream = null;
    private GBDevice mGbDevice;
    private AbstractBTBRDeviceSupport mSupport;

    private final BlockingQueue<AbstractTransaction> mTransactions = new LinkedBlockingQueue<>();
    private volatile boolean mDisposed;
    private volatile boolean mCrashed;
    private volatile boolean mAbortTransaction;

    private Context mContext;
    private CountDownLatch mConnectionLatch;
    private BluetoothSocketCharacteristic mWaitCharacteristic;
    private CountDownLatch mWaitForActionResultLatch;
    private final InternalSocketCallback internalSocketCallback;

    private Thread dispatchThread = new Thread("Gadgetbridge IO dispatcher") {
        @Override
        public void run() {
            LOG.debug("Socket Dispatch Thread started.");
            
            while (!mDisposed && !mCrashed) {
                try {

                    AbstractTransaction qTransaction = mTransactions.take();

                    //try {

                        if (!isConnected()) {
                            LOG.debug("Not connected, waiting for connection...");
                            setDeviceConnectionState(GBDevice.State.NOT_CONNECTED);
                            internalSocketCallback.reset();
                            // wait until the connection succeeds before running the actions
                            // Note that no automatic connection is performed. This has to be triggered
                            // on the outside typically by the DeviceSupport. The reason is that
                            // devices have different kinds of initializations and this class has no
                            // idea about them.
                            mConnectionLatch = new CountDownLatch(1);
                            mConnectionLatch.await();
                            mConnectionLatch = null;
                        }
                        LOG.info("Ready for a new message exchange.");
                        Transaction transaction = (Transaction)qTransaction;
                        internalSocketCallback.setTransactionSocketCallback(transaction.getSocketCallback());
                        mAbortTransaction = false;
                        for (BtBRAction action : transaction.getActions()) {
                            if (mAbortTransaction) {
                                LOG.info("Abording running transaction");
                            }
                            mWaitCharacteristic = action.getCharacteristic();
                            mWaitForActionResultLatch = new CountDownLatch(1);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("About to run action: " + action);
                            }
                            if (action.run(mBtSocket)) {
                                boolean waitForResult = action.expectsResult();
                                if (waitForResult) {
                                    mWaitForActionResultLatch.await();
                                    mWaitForActionResultLatch = null;
                                    if (mAbortTransaction) {
                                        break;
                                    }
                                }
                            } else {
                                LOG.error("Action returned false: " + action);
                                break;
                            }
                        }
                    //} catch (SocketTimeoutException ignore) {
                    //    LOG.debug("socket timeout, we can't help but ignore this");
                    //} catch (IOException e) {
                    //    LOG.info(e.getMessage());
                    //    mBtSocket = null;
                        //mInStream = null;
                        //mOutStream = null;
                    //    LOG.info("Bluetooth socket closed, will quit IO Thread");
                    //    break;
                    //}
                    setDeviceConnectionState(GBDevice.State.NOT_CONNECTED);
                }  catch (InterruptedException ignored) {
                    mConnectionLatch = null;
                    LOG.debug("Thread interrupted");
                } catch (Throwable ex) {
                    LOG.error("IO Dispatch Thread died: " + ex.getMessage(), ex);
                    mCrashed = true;
                    mConnectionLatch = null;
                }
            }
        }
    };

    public BtBRQueue(BluetoothAdapter btAdapter, GBDevice gbDevice, Context context, SocketCallback externalSocketCallback, AbstractBTBRDeviceSupport support) {
        mBtAdapter = btAdapter;
        mGbDevice = gbDevice;
        mContext = context;
        internalSocketCallback = new InternalSocketCallback(externalSocketCallback);
        mSupport = support;

        dispatchThread.start();
    }

    /**
     * Connects to the given remote device. Note that this does not perform any device
     * specific initialization. This should be done in the specific {@link DeviceSupport}
     * class.
     *
     * @return <code>true</code> whether the connection attempt was successfully triggered and <code>false</code> if that failed or if there is already a connection
     */

    protected boolean connect() {
        if (isConnected()) {
            LOG.warn("Ignoring connect() because already connected.");
            return false;
        }

        LOG.info("Attemping to connect to " + mGbDevice.getName());
        GBDevice.State originalState = mGbDevice.getState();
        setDeviceConnectionState(GBDevice.State.CONNECTING);

        try {
            BluetoothDevice btDevice = mBtAdapter.getRemoteDevice(mGbDevice.getAddress());
            ParcelUuid uuids[] = btDevice.getUuids();
            if (uuids == null) {
                LOG.warn("Device provided no UUIDs to connect to, giving up: " + mGbDevice);
                return false;
            }
            for (ParcelUuid uuid : uuids) {
                LOG.info("found service UUID " + uuid);
            }
            mBtSocket = btDevice.createRfcommSocketToServiceRecord(mSupport.getSupportedService());
            mBtSocket.connect();
            setDeviceConnectionState(GBDevice.State.CONNECTED);
            if (mConnectionLatch != null) {
                mConnectionLatch.countDown();
            }
        } catch (IOException e) {
            LOG.error("Server socket cannot be started.", e);
            setDeviceConnectionState(originalState);
            //mInStream = null;
            //mOutStream = null;
            mBtSocket = null;
            return false;
        }

        return true;
    }

    public void disconnect() {
        if (mBtSocket != null) {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    protected boolean isConnected() {
        return mGbDevice.isConnected();
    }

    /**
     * Adds a transaction to the end of the queue.
     *
     * @param transaction
     */
    public void add(Transaction transaction) {
        LOG.debug("about to add: " + transaction);
        if (!transaction.isEmpty()) {
            mTransactions.add(transaction);
        }
    }

    /**
     * Adds a transaction to the beginning of the queue.
     * Note that actions of the *currently executing* transaction
     * will still be executed before the given transaction.
     *
     * @param transaction
     */
    public void insert(Transaction transaction) {
        LOG.debug("about to insert: " + transaction);
        if (!transaction.isEmpty()) {
            List<AbstractTransaction> tail = new ArrayList<>(mTransactions.size() + 2);
            //mTransactions.drainTo(tail);
            tail.addAll(mTransactions);
            mTransactions.clear();
            mTransactions.add(transaction);
            mTransactions.addAll(tail);
        }
    }
    public void clear() {
        mTransactions.clear();
    }

    protected void setDeviceConnectionState(GBDevice.State newState) {
        LOG.debug("New device connection state: " + newState);
        mGbDevice.setState(newState);
        mGbDevice.sendDeviceUpdateIntent(mContext);
    }

    public void dispose() {
        if (mDisposed) {
            return;
        }
        mDisposed = true;
        disconnect();
        dispatchThread.interrupt();
        dispatchThread = null;
    }

    // Implements callback methods for IO events that the app cares about.  For example,
    // connection change.
    private final class InternalSocketCallback extends BluetoothSocketCallback {
        private
        @Nullable
        SocketCallback mTransactionSocketCallback;
        private final SocketCallback mExternalSocketCallback;

        public InternalSocketCallback(SocketCallback externalSocketCallback) {
            mExternalSocketCallback = externalSocketCallback;
        }

        public void setTransactionSocketCallback(@Nullable SocketCallback callback) {
            mTransactionSocketCallback = callback;
        }

        private SocketCallback getCallbackToUse() {
            if (mTransactionSocketCallback != null) {
                return mTransactionSocketCallback;
            }
            return mExternalSocketCallback;
        }

        @Override
        public void onSocketWrite(BluetoothSocketCharacteristic characteristic) {
            LOG.debug("characteristic write: " + characteristic.getUuid());
            if (getCallbackToUse() != null) {
                getCallbackToUse().onSocketWrite(characteristic);
            }
            checkWaitingCharacteristic(characteristic); 
        }

        @Override
        public void onSocketRead(BluetoothSocketCharacteristic characteristic) {
           LOG.debug("characteristic read: " + characteristic.getUuid());
           if (getCallbackToUse() != null) {
               try {
                   getCallbackToUse().onSocketRead(characteristic);
               } catch (Throwable ex) {
                   LOG.error("onSocketRead: " + ex.getMessage(), ex);
               }
           }
        }

        private void checkWaitingCharacteristic(BluetoothSocketCharacteristic characteristic) {
                if (characteristic == null) {
                    LOG.debug("failed btbr action, aborting transaction");
                }
                mAbortTransaction = true;
            if (characteristic != null && BtBRQueue.this.mWaitCharacteristic != null && characteristic.getUuid().equals(BtBRQueue.this.mWaitCharacteristic.getUuid())) {
                if (mWaitForActionResultLatch != null) {
                    mWaitForActionResultLatch.countDown();
                }
            } else {
                if (BtBRQueue.this.mWaitCharacteristic != null) {
                    LOG.error("checkWaitingCharacteristic: mismatched characteristic received: " + ((characteristic != null && characteristic.getUuid() != null) ? characteristic.getUuid().toString() : "(null)"));
                }
            }
        }

        public void reset() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("internal io callback set to null");
            }
            mTransactionSocketCallback = null;
        }
    }


}
