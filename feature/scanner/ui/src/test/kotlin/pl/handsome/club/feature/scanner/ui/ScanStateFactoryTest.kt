package pl.handsome.club.feature.scanner.ui

import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.scanner.ui.state.CameraPermission
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

class ScanStateFactoryTest {

    private val factory = ScanStateFactory(ScanResources())

    @Test
    fun `when permission is unchecked or requesting then camera is hidden`() {
        assertEquals(CameraState.Hidden, camera(CameraPermission.Unchecked))
        assertEquals(CameraState.Hidden, camera(CameraPermission.Requesting))
    }

    @Test
    fun `when permission is granted and nothing pauses then preview is not paused`() {
        assertEquals(CameraState.Preview(isPaused = false), camera(CameraPermission.Granted))
    }

    @Test
    fun `when permission is granted and scan is paused then preview is paused`() {
        assertEquals(CameraState.Preview(isPaused = true), camera(CameraPermission.Granted, isScanPaused = true))
    }

    @Test
    fun `when permission is granted and a search runs then preview is paused`() {
        assertEquals(CameraState.Preview(isPaused = true), camera(CameraPermission.Granted, isSearching = true))
    }

    @Test
    fun `when permission needs rationale then panel shows rationale and allow camera`() {
        assertEquals(
            CameraState.PermissionPanel(
                message = UiText.Res(R.string.scan_camera_permission_rationale),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
            ),
            camera(CameraPermission.Rationale),
        )
    }

    @Test
    fun `when permission is denied then panel shows denied message and open settings`() {
        assertEquals(
            CameraState.PermissionPanel(
                message = UiText.Res(R.string.scan_camera_permission_denied),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
            ),
            camera(CameraPermission.Denied),
        )
    }

    @Test
    fun `when searching or not then progress follows`() {
        assertTrue(factory.createFor(Prototype(isSearching = true)).showProgress)
        assertFalse(factory.createFor(Prototype(isSearching = false)).showProgress)
    }

    @Test
    fun `when query is set then it is passed through unchanged`() {
        assertEquals(" 590 ", factory.createFor(Prototype(query = " 590 ")).query)
    }

    private fun camera(
        permission: CameraPermission,
        isScanPaused: Boolean = false,
        isSearching: Boolean = false,
    ): CameraState = factory.createFor(
        Prototype(cameraPermission = permission, isScanPaused = isScanPaused, isSearching = isSearching)
    ).camera
}
