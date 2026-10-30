package pl.handsome.club.feature.scanner.ui

import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.scanner.ui.state.CameraPermission
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import pl.handsome.club.feature.scanner.ui.state.Torch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

class ScanStateFactoryTest {

    private val factory = ScanStateFactory(ScanResources())

    @Test
    fun `when permission is unchecked or requesting then camera is hidden`() {
        assertEquals(CameraState.Hidden, camera(Prototype(cameraPermission = CameraPermission.Unchecked)))
        assertEquals(CameraState.Hidden, camera(Prototype(cameraPermission = CameraPermission.Requesting)))
    }

    @Test
    fun `when permission is granted and nothing pauses then preview is not paused`() {
        assertEquals(preview(isPaused = false), camera(granted()))
    }

    @Test
    fun `when permission is granted and scan is paused then preview is paused`() {
        assertEquals(preview(isPaused = true), camera(granted().copy(isScanPaused = true)))
    }

    @Test
    fun `when permission is granted and a search runs then preview is paused`() {
        assertEquals(preview(isPaused = true), camera(granted().copy(isSearching = true)))
    }

    @Test
    fun `when the type sheet is open then preview shows it and is paused`() {
        assertEquals(
            preview(isPaused = true, isTypeSheetOpen = true),
            camera(granted().copy(isTypeSheetOpen = true)),
        )
    }

    @Test
    fun `when the device has no torch then torch is unavailable even if it was turned on`() {
        assertEquals(preview(torch = Torch.Unavailable), camera(granted().copy(hasTorch = false, isTorchOn = true)))
    }

    @Test
    fun `when the device has a torch then torch follows its on state`() {
        assertEquals(preview(torch = Torch.Off), camera(granted().copy(hasTorch = true, isTorchOn = false)))
        assertEquals(preview(torch = Torch.On), camera(granted().copy(hasTorch = true, isTorchOn = true)))
    }

    @Test
    fun `when permission needs rationale then panel shows rationale title message and allow camera`() {
        assertEquals(
            CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_rationale_title),
                message = UiText.Res(R.string.scan_camera_permission_rationale),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
            ),
            camera(Prototype(cameraPermission = CameraPermission.Rationale)),
        )
    }

    @Test
    fun `when permission is denied then panel shows denied title message and open settings`() {
        assertEquals(
            CameraState.PermissionPanel(
                title = UiText.Res(R.string.scan_camera_permission_denied_title),
                message = UiText.Res(R.string.scan_camera_permission_denied),
                buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
            ),
            camera(Prototype(cameraPermission = CameraPermission.Denied)),
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

    private fun granted() = Prototype(cameraPermission = CameraPermission.Granted)

    private fun preview(isPaused: Boolean = false, torch: Torch = Torch.Unavailable, isTypeSheetOpen: Boolean = false) =
        CameraState.Preview(isPaused = isPaused, torch = torch, isTypeSheetOpen = isTypeSheetOpen)

    private fun camera(prototype: Prototype): CameraState = factory.createFor(prototype).camera
}
