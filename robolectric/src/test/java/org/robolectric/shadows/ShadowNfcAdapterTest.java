package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowNfcAdapterTest {

  private final Application context = RuntimeEnvironment.getApplication();

  @Before
  public void setUp() throws Exception {
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_NFC, /* supported= */ true);
  }

  @Test
  public void setNdefPushMesageCallback_shouldUseCallback() {
    final NfcAdapter.CreateNdefMessageCallback callback =
        mock(NfcAdapter.CreateNdefMessageCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setNdefPushMessageCallback(callback, activity);
    assertThat(shadowOf(adapter).getNdefPushMessageCallback()).isSameInstanceAs(callback);
  }

  @Test
  public void setOnNdefPushCompleteCallback_shouldUseCallback() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setOnNdefPushCompleteCallback(callback, activity);
    assertThat(shadowOf(adapter).getOnNdefPushCompleteCallback()).isSameInstanceAs(callback);
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullActivity() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final Activity nullActivity = null;
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> adapter.setOnNdefPushCompleteCallback(callback, nullActivity));
    assertThat(exception).hasMessageThat().contains("activity cannot be null");
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullInActivities() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final Activity nullActivity = null;
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> adapter.setOnNdefPushCompleteCallback(callback, activity, nullActivity));
    assertThat(exception).hasMessageThat().contains("activities cannot contain null");
  }

  @Test
  public void isEnabled_shouldReturnEnabledState() {
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    assertThat(adapter.isEnabled()).isFalse();

    shadowOf(adapter).setEnabled(true);
    assertThat(adapter.isEnabled()).isTrue();

    shadowOf(adapter).setEnabled(false);
    assertThat(adapter.isEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isSecureNfcSupported_shouldReturnSupportedState() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    assertThat(adapter.isSecureNfcSupported()).isFalse();

    shadowOf(adapter).setSecureNfcSupported(true);
    assertThat(adapter.isSecureNfcSupported()).isTrue();

    shadowOf(adapter).setSecureNfcSupported(false);
    assertThat(adapter.isSecureNfcSupported()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isSecureNfcEnabled_shouldReturnEnabledState() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    assertThat(adapter.isSecureNfcEnabled()).isFalse();

    adapter.enableSecureNfc(true);
    assertThat(adapter.isSecureNfcEnabled()).isTrue();

    adapter.enableSecureNfc(false);
    assertThat(adapter.isSecureNfcEnabled()).isFalse();
  }

  @Test
  public void getNfcAdapter_returnsNonNull() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNotNull();

    // This is checked twice to prevent a regression where attempting to acquire the
    // `getDefaultAdapter` twice in a test would cause a `null` value to be returned on UDC+.
    NfcAdapter adapterAgain = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapterAgain).isNotNull();
  }

  @Test
  public void getNfcAdapter_hardwareExists_returnsNonNull() {
    ShadowNfcAdapter.setNfcHardwareExists(true);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNotNull();
  }

  @Test
  public void getNfcAdapter_hardwareDoesNotExist_returnsNull() {
    ShadowNfcAdapter.setNfcHardwareExists(false);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNull();
  }

  @Test
  public void setNdefPushMessage_setsNullMessage() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setNdefPushMessage(null, activity);

    assertThat(shadowOf(adapter).getNdefPushMessage()).isNull();
  }

  @Test
  public void setNdefPushMessage_setsNonNullMessage() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    final NdefMessage message =
        new NdefMessage(new NdefRecord[] {new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)});

    adapter.setNdefPushMessage(message, activity);

    assertThat(shadowOf(adapter).getNdefPushMessage()).isSameInstanceAs(message);
  }

  @Test
  public void getNdefPushMessage_messageNotSet_throwsIllegalStateException() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    assertThrows(IllegalStateException.class, () -> shadowOf(adapter).getNdefPushMessage());
  }

  @Test
  public void isInReaderMode_beforeEnableReaderMode_shouldReturnFalse() {
    final Activity activity = Robolectric.setupActivity(Activity.class);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    assertThat(shadowOf(adapter).isInReaderMode()).isFalse();
  }

  @Test
  public void isInReaderMode_afterEnableReaderMode_shouldReturnTrue() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
    adapter.enableReaderMode(
        activity,
        callback,
        NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
        /* extras= */ null);

    assertThat(shadowOf(adapter).isInReaderMode()).isTrue();
  }

  @Test
  public void isInReaderMode_afterDisableReaderMode_shouldReturnFalse() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
    adapter.enableReaderMode(
        activity,
        callback,
        NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
        /* extras= */ null);
    adapter.disableReaderMode(activity);

    assertThat(shadowOf(adapter).isInReaderMode()).isFalse();
  }

  @Test
  public void dispatchTagDiscovered_shouldDispatchTagToCallback() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
    adapter.enableReaderMode(
        activity,
        callback,
        NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
        /* extras= */ null);
    Tag tag = ShadowNfcAdapter.createMockTag();
    shadowOf(adapter).dispatchTagDiscovered(tag);

    verify(callback).onTagDiscovered(same(tag));
  }
}
