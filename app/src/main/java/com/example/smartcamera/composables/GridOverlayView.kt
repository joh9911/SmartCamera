import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {

        val gridColor = Color.Gray.copy(alpha = 0.5f) // 50% 투명한 회색
        val numColumns = 3
        val numRows = 3
        val cellWidth = size.width / numColumns
        val cellHeight = size.height / numRows

        for (i in 1 until numColumns) {
            drawLine(
                start = Offset(x = cellWidth * i, y = 0f),
                end = Offset(x = cellWidth * i, y = size.height),
                color = gridColor,
                strokeWidth = 1.dp.toPx()
            )
        }

        for (i in 1 until numRows) {
            drawLine(
                start = Offset(x = 0f, y = cellHeight * i),
                end = Offset(x = size.width, y = cellHeight * i),
                color = gridColor,
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
