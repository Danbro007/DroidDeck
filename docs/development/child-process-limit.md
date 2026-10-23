# Child-process checks on OEM Android ROMs

Issues [#713](https://github.com/Droid-Deck/DroidDeck/issues/713) and
[#1083](https://github.com/Droid-Deck/DroidDeck/issues/1083) report ROMs that reject
the global monitor-setting write or omit its Developer options switch. Increasing
`activity_manager/max_phantom_processes` over ADB can still work on these ROMs.

## Detection and verification

- Android 12 (SDK 31) requires a verified process-count setting. Its original
  AOSP implementation does not use the monitor flag to guard count trimming.
- Android 12L and later accept either a disabled monitor flag or a verified
  `max_phantom_processes` value of `2147483647`.
- A readable global monitor setting takes precedence over its system-property
  override. A failed global read does not establish that an override is effective.
- Missing values, permission errors, and failed commands do not establish that
  restrictions have been removed. Overflow and malformed count values are rejected.
- Raising the count limit does not disable every Android process restriction,
  including the separate excessive-background-CPU check.

Some Android versions and OEM ROMs restrict the Activity Manager DeviceConfig
namespace. `READ_DEVICE_CONFIG` is a signature/preinstalled permission, not a
permission a user can grant to a sideloaded APK with `pm grant`. A direct read is
attempted; permission failure remains unknown rather than being treated as
success. The Android 16 Google APIs emulator used for validation allowed this
read, so its max-only cold-start result does not exercise the permission-denied
fallback.

Wireless ADB tries the two setting writes independently, then reads the global
flag, its property override, and the process-count setting. Success depends on
the readback, not on a command merely exiting successfully. Restoring restrictions
also requires verification of both applicable controls. The repair does not
change DeviceConfig's global synchronization policy.

A verified shell snapshot bridges only the otherwise unreadable DeviceConfig
count. Global flags and their property override always use current app reads.
The snapshot is held only in the current app process for at most 60 seconds.
Its SDK, build fingerprint, and app-visible settings must still match.
A new verification clears the previous snapshot; failures cannot reuse an earlier
success. The old persistent Android 12 acknowledgement is no longer trusted.

When a saved pairing exists and a local check blocks Steam, the launcher retries
a read-only shell check off the UI thread, at most once every 30 seconds. On ROMs
that hide DeviceConfig, a computer-only ADB change still needs Wireless debugging
verification in DroidDeck. The gate exposes that action even on Android versions
that normally have a Developer options switch.

## Sources

- [Android 12 count trimming](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-12.0.0_r1/services/core/java/com/android/server/am/PhantomProcessList.java#L421)
- [Android 12L monitor guard](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-12.1.0_r1/services/core/java/com/android/server/am/PhantomProcessList.java#L423)
- [Global flag and property precedence](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/util/FeatureFlagUtils.java#L139)
- [DeviceConfig read permission](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/java/android/provider/DeviceConfig.java#L1198)
- [Permission protection level](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/core/res/AndroidManifest.xml#L3490)
- [Separate background CPU check](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/services/core/java/com/android/server/am/ActivityManagerService.java#L15196)

## Validation

Run `PhantomProcessLimitTest`, `WirelessAdbFixTest`, and
`PhantomProcessGatePageTest` with Gradle's `testDebugUnitTest` task. These cover
reported readback values, independent write failures, strict restoration,
short-lived verification, and access to Wireless debugging on newer ROMs.

These tests do not establish physical-device compatibility. HyperOS/MIUI and
Realme UI still need device testing for write permissions, pairing, repeated
launches, Developer options changes, and a device reboot.
