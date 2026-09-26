package com.deniz0706.ykstakip.ui.statistics

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.deniz0706.ykstakip.R

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Point(
        val xLabel: String,
        val value: Float,
        val title: String,
        val fullDate: String,
        val unit: String
    )

    var points: List<Point> = emptyList()
        set(value) {
            field = value
            selectedIndex = null
            requestLayout()
            invalidate()
        }

    var targetValue: Float? = null
        set(value) {
            field = value
            invalidate()
        }

    var reverseY: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private var selectedIndex: Int? = null

    private val lineColor = ContextCompat.getColor(context, R.color.accent)
    private val gridColor = ContextCompat.getColor(context, R.color.border)
    private val textColor = ContextCompat.getColor(context, R.color.on_surface_secondary)
    private val labelColor = ContextCompat.getColor(context, R.color.on_background)
    private val surfaceColor = ContextCompat.getColor(context, R.color.surface)
    private val targetColor = ContextCompat.getColor(context, R.color.positive)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        alpha = 30
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        style = Paint.Style.FILL
    }

    private val dotHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = surfaceColor
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gridColor
        strokeWidth = 2f
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 26f
    }

    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = surfaceColor
        setShadowLayer(12f, 0f, 4f, 0x33000000)
    }

    private val tooltipTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelColor
        textSize = 26f
        isFakeBoldText = true
    }

    private val tooltipBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 24f
    }

    private val targetLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = targetColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(14f, 10f), 0f)
    }

    private val targetTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = targetColor
        textSize = 24f
        isFakeBoldText = true
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val width = MeasureSpec.getSize(widthMeasure)
        val height =
            (width * 0.62f)
                .toInt()
                .coerceAtLeast(dp(180))

        setMeasuredDimension(width, height)
    }

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (points.isEmpty()) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                selectedIndex = nearestIndex(event.x)
                invalidate()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun chartRect(): RectF {
        val padLeft = 60f
        val padRight = 12f
        val padTop = 30f
        val padBottom = 50f

        return RectF(
            padLeft,
            padTop,
            width - padRight,
            height - padBottom
        )
    }

    private fun nearestIndex(touchX: Float): Int {
        val rect = chartRect()

        if (points.size == 1) return 0

        val stepX =
            rect.width() / (points.size - 1)

        val idx =
            ((touchX - rect.left) / stepX)
                .toInt()
                .coerceIn(0, points.size - 1)

        return idx
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isEmpty()) return

        val rect = chartRect()

        val values =
            points.map { it.value }.toMutableList()

        targetValue?.let {
            values.add(it)
        }

        var minV = values.min()
        var maxV = values.max()

        if (minV == maxV) {
            minV -= 1f
            maxV += 1f
        }

        val rangePad =
            (maxV - minV) * 0.12f

        minV -= rangePad
        maxV += rangePad

        if (!reverseY && minV > 0f) {
            minV = 0f
        }

        for (i in 0..3) {
            val y =
                rect.top +
                    rect.height() * i / 3f

            canvas.drawLine(
                rect.left,
                y,
                rect.right,
                y,
                gridPaint
            )

            val value =
                if (reverseY) {
                    minV + (maxV - minV) * i / 3f
                } else {
                    maxV - (maxV - minV) * i / 3f
                }

            val text =
                value.toInt().toString()

            val textWidth =
                axisTextPaint.measureText(text)

            canvas.drawText(
                text,
                rect.left - textWidth - 8f,
                y + axisTextPaint.textSize / 3f,
                axisTextPaint
            )
        }

        fun xFor(i: Int): Float =
            if (points.size == 1) {
                rect.centerX()
            } else {
                rect.left +
                    rect.width() * i / (points.size - 1)
            }

        fun yFor(v: Float): Float =
            if (reverseY) {
                rect.top +
                    (v - minV) /
                    (maxV - minV) *
                    rect.height()
            } else {
                rect.bottom -
                    (v - minV) /
                    (maxV - minV) *
                    rect.height()
            }

        targetValue?.let { target ->
            val y = yFor(target)

            canvas.drawLine(
                rect.left,
                y,
                rect.right,
                y,
                targetLinePaint
            )

            val label =
                "Hedef: ${formatValue(target)}"

            val labelWidth =
                targetTextPaint.measureText(label)

            val labelY =
                if (y - 10f < rect.top + 20f) {
                    y + 26f
                } else {
                    y - 10f
                }

            canvas.drawText(
                label,
                rect.right - labelWidth,
                labelY,
                targetTextPaint
            )
        }

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, p ->
            val x = xFor(i)
            val y = yFor(p.value)

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, rect.bottom)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(
            xFor(points.size - 1),
            rect.bottom
        )

        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)

        points.forEachIndexed { i, p ->
            val x = xFor(i)
            val y = yFor(p.value)

            val radius =
                if (i == selectedIndex) 12f else 7f

            canvas.drawCircle(
                x,
                y,
                radius + 4f,
                dotHaloPaint
            )

            canvas.drawCircle(
                x,
                y,
                radius,
                dotPaint
            )
        }

        val firstLabel =
            points.first().xLabel

        val lastLabel =
            points.last().xLabel

        canvas.drawText(
            firstLabel,
            rect.left,
            height - 14f,
            axisTextPaint
        )

        val lastWidth =
            axisTextPaint.measureText(lastLabel)

        canvas.drawText(
            lastLabel,
            rect.right - lastWidth,
            height - 14f,
            axisTextPaint
        )

        selectedIndex?.let { idx ->
            val p = points[idx]

            val x = xFor(idx)
            val y = yFor(p.value)

            val title = p.title

            val body =
                "${p.fullDate} — ${formatValue(p.value)}${p.unit}"

            val titleW =
                tooltipTitlePaint.measureText(title)

            val bodyW =
                tooltipBodyPaint.measureText(body)

            val boxW =
                maxOf(titleW, bodyW) + 32f

            val boxH = 76f

            var boxLeft =
                x - boxW / 2f

            if (boxLeft < 0f) {
                boxLeft = 4f
            }

            if (boxLeft + boxW > width) {
                boxLeft =
                    width - boxW - 4f
            }

            var boxTop =
                y - boxH - 20f

            if (boxTop < 0f) {
                boxTop = y + 20f
            }

            canvas.drawRoundRect(
                boxLeft,
                boxTop,
                boxLeft + boxW,
                boxTop + boxH,
                14f,
                14f,
                tooltipBgPaint
            )

            canvas.drawText(
                title,
                boxLeft + 16f,
                boxTop + 30f,
                tooltipTitlePaint
            )

            canvas.drawText(
                body,
                boxLeft + 16f,
                boxTop + 58f,
                tooltipBodyPaint
            )
        }
    }

    private fun formatValue(v: Float): String =
        if (v == v.toInt().toFloat()) {
            v.toInt().toString()
        } else {
            String.format(
                java.util.Locale.US,
                "%.2f",
                v
            ).replace('.', ',')
        }
}
