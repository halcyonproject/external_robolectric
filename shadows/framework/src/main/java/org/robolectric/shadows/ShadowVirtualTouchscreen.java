package org.robolectric.shadows;

import android.hardware.input.VirtualTouchEvent;
import android.hardware.input.VirtualTouchscreen;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualTouchscreen. */
@Implements(
    value = VirtualTouchscreen.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowVirtualTouchscreen {

  private final AtomicBoolean isClosed = new AtomicBoolean(false);
  private final List<VirtualTouchEvent> sentEvents = new ArrayList<>();

  @Implementation
  protected void close() {
    isClosed.set(true);
  }

  public boolean isClosed() {
    return isClosed.get();
  }

  @Implementation
  protected void sendTouchEvent(VirtualTouchEvent event) {
    sentEvents.add(event);
  }

  public List<VirtualTouchEvent> getSentEvents() {
    return sentEvents;
  }
}
