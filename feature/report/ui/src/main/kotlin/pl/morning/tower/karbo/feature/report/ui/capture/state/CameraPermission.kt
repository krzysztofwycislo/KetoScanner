package pl.morning.tower.karbo.feature.report.ui.capture.state

internal enum class CameraPermission { Unchecked, Requesting, Granted, Rationale, Denied }

internal data class CameraPermissionCheck(val isGranted: Boolean, val shouldShowRationale: Boolean)
