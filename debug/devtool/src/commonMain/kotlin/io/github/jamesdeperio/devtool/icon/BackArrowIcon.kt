package io.github.jamesdeperio.devtool.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val BackIcon: ImageVector =
    ImageVector
        .Builder(
            name = "BackIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9.0f, 6.0f) // top left of head
                lineTo(10.41f, 7.41f) // slope down to mid
                lineTo(6.83f, 11.0f) // left slope toward tail
                lineTo(20.0f, 11.0f) // tail goes right
                lineTo(20.0f, 13.0f) // tail thickness
                lineTo(6.83f, 13.0f) // back to head
                lineTo(10.41f, 16.59f) // slope up
                lineTo(9.0f, 18.0f) // bottom left of head
                lineTo(3.0f, 12.0f) // arrow point (left)
                close()
            }
        }.build()
