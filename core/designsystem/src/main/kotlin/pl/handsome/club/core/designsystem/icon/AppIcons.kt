package pl.handsome.club.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import pl.handsome.club.core.designsystem.R

@JvmInline
value class AppIcon(@DrawableRes val id: Int)

val AppIcon.imageVector: ImageVector
    @Composable get() = ImageVector.vectorResource(id)

object AppIcons {
    val Back = AppIcon(R.drawable.ic_back)
    val Home = AppIcon(R.drawable.ic_home)
    val Scan = AppIcon(R.drawable.ic_scan)
    val Heart = AppIcon(R.drawable.ic_heart)
    val HeartFilled = AppIcon(R.drawable.ic_heart_filled)
    val Check = AppIcon(R.drawable.ic_check)
    val Minus = AppIcon(R.drawable.ic_minus)
    val Cross = AppIcon(R.drawable.ic_cross)
    val Question = AppIcon(R.drawable.ic_question)
    val ChevronRight = AppIcon(R.drawable.ic_chevron_right)
    val Torch = AppIcon(R.drawable.ic_torch)
    val Keyboard = AppIcon(R.drawable.ic_keyboard)
    val CameraOff = AppIcon(R.drawable.ic_camera_off)
    val Bag = AppIcon(R.drawable.ic_bag)
    val Logo = AppIcon(R.drawable.ic_logo)
    val Sliders = AppIcon(R.drawable.ic_sliders)
    val Info = AppIcon(R.drawable.ic_info)
    val Search = AppIcon(R.drawable.ic_search)
    val Jar = AppIcon(R.drawable.ic_jar)
    val Bottle = AppIcon(R.drawable.ic_bottle)
    val Can = AppIcon(R.drawable.ic_can)
    val Carton = AppIcon(R.drawable.ic_carton)
    val Cup = AppIcon(R.drawable.ic_cup)
    val Bar = AppIcon(R.drawable.ic_bar)
    val Cheese = AppIcon(R.drawable.ic_cheese)
    val Egg = AppIcon(R.drawable.ic_egg)
    val Fish = AppIcon(R.drawable.ic_fish)
    val Nut = AppIcon(R.drawable.ic_nut)
    val Grain = AppIcon(R.drawable.ic_grain)
    val Cookie = AppIcon(R.drawable.ic_cookie)
    val Leaf = AppIcon(R.drawable.ic_leaf)
    val Meat = AppIcon(R.drawable.ic_meat)
}
