package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static java.util.Objects.requireNonNull;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManagerCallback;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.ForType;
import android.os.HandlerThread;
import org.robolectric.util.reflector.Reflector;

/** Holds fakes for the IBluetoothManager system service */
class IBluetoothManagerDelegates {

  private IBluetoothManagerDelegates() {}

  /** Creates the appropriate delegate to use based on API level. */
  static Object createDelegate() {
    if (RuntimeEnvironment.getApiLevel() < TIRAMISU) {
      // android T introduces new classes which result in NoClassDefFoundErrors if loaded in older
      // API levels
      return new IBluetoothManagerDelegateS();
    } else {
      return new IBluetoothManagerDelegate();
    }
  }

  private static class IBluetoothManagerDelegateBase {
    private IBluetoothGatt iBluetoothGatt;

    public IBluetoothGatt getBluetoothGatt() {
      if (iBluetoothGatt == null) {
        iBluetoothGatt = BluetoothGattProxyDelegate.createBluetoothGattProxy();
      }
      return iBluetoothGatt;
    }
  }

  private static class IBluetoothManagerDelegateS extends IBluetoothManagerDelegateBase {

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
      IBinder btBinder = requireNonNull(ServiceManager.getService(Context.BLUETOOTH_SERVICE));
      IBluetooth btService = requireNonNull(IBluetooth.Stub.asInterface(btBinder));
      Reflector.reflector(IBluetoothManagerCallbackReflectorS.class, callback)
          .onBluetoothServiceUp(btService);
      return btService;
    }
  }

  @ForType(IBluetoothManagerCallback.class)
  private interface IBluetoothManagerCallbackReflectorS {
    void onBluetoothServiceUp(IBluetooth bluetoothService);
  }

  // Any BluetoothAdapter calls which need to invoke BluetoothManager methods can delegate those
  // calls to this class. The default behavior for any methods not defined in this class is a no-op.
  @SuppressWarnings("unused")
  private static class IBluetoothManagerDelegate extends IBluetoothManagerDelegateBase {

    private IBluetoothManagerDelegate() {}

    /** invoke the service connected callback. */
    boolean bindBluetoothProfileService(BluetoothProfile proxy) {
      if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
        return false;
      }
      proxy.onServiceConnected(null);
      return true;
    }

    /** invoke the service disconnected callback. */
    void unbindBluetoothProfileService(BluetoothProfile proxy) {
      proxy.onServiceDisconnected();
    }

    public Messenger getServiceMessenger() {
      var thread = new HandlerThread("BluetoothSystemServerMessenger");
      thread.start();
      Looper looper = thread.getLooper();

      Handler handler =
          new Handler(
              looper,
              msg -> {
                if (msg.replyTo == null) {
                  return true;
                }
                try {
                  Object data = msg.obj;
                  String requestClassName = data.getClass().getName();
                  // SystemServiceMessage classes are not public, so we can't import them.
                  // Instead, we rely on the naming convention of the reply class.
                  Class<?> replyClass = Class.forName(requestClassName + "$Reply");
                  Object replyData = replyClass.getConstructor().newInstance();

                  // The default constructor will leave the IBluetooth field as null,
                  // which simulates the Bluetooth-off state.

                  android.os.Message replyMsg = android.os.Message.obtain();
                  replyMsg.obj = replyData;
                  msg.replyTo.send(replyMsg);
                } catch (Exception e) {
                  // Don't crash the test host.
                }
                return true;
              });
      return new Messenger(handler);
    }
  }
}
