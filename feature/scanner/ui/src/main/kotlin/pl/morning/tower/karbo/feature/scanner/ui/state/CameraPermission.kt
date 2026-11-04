package pl.morning.tower.karbo.feature.scanner.ui.state

internal enum class CameraPermission { Unchecked, Requesting, Granted, Rationale, Denied }

internal data class CameraPermissionCheck(val isGranted: Boolean, val shouldShowRationale: Boolean)
