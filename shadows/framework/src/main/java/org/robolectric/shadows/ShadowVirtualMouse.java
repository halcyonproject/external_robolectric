package org.robolectric.shadows;

import android.hardware.input.VirtualMouse;
import android.hardware.input.VirtualMouseButtonEvent;
import android.hardware.input.VirtualMouseRelativeEvent;
import android.hardware.input.VirtualMouseScrollEvent;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for VirtualMouse. */
@Implements(value = VirtualMouse.class, minSdk = VERSION_CODES.TIRAMISU, isInAndroidSdk = false)
public class ShadowVirtualMouse {

  private final AtomicBoolean isClosed = new AtomicBoolean(false);
  private final List<VirtualMouseButtonEvent> sentButtonEvents = new ArrayList<>();
  private final List<VirtualMouseScrollEvent> sentScrollEvents = new ArrayList<>();
  private final List<VirtualMouseRelativeEvent> sentRelativeEvents = new ArrayList<>();

  @Implementation
  protected void close() {
    isClosed.set(true);
  }

  public boolean isClosed() {
    return isClosed.get();
  }

  @Implementation
  protected void sendButtonEvent(VirtualMouseButtonEvent event) {
    sentButtonEvents.add(event);
  }

  @Implementation
  protected void sendScrollEvent(VirtualMouseScrollEvent event) {
    sentScrollEvents.add(event);
  }

  @Implementation
  protected void sendRelativeEvent(VirtualMouseRelativeEvent event) {
    sentRelativeEvents.add(event);
  }

  public List<VirtualMouseButtonEvent> getSentButtonEvents() {
    return sentButtonEvents;
  }

  public List<VirtualMouseScrollEvent> getSentScrollEvents() {
    return sentScrollEvents;
  }

  public List<VirtualMouseRelativeEvent> getSentRelativeEvents() {
    return sentRelativeEvents;
  }
}
