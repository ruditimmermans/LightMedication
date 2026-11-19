package com.light.medication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LightToggle(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = if (isCompact) 44.dp else 48.dp)
            .alpha(alpha)
            .toggleable(
                value = isChecked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleMarkCanvas(
            isChecked = isChecked,
            isCompact = isCompact,
            color = color,
            modifier = Modifier.padding(end = if (isCompact) 12.dp else 18.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = if (isCompact) 16.sp else 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleMarkCanvas(
    isChecked: Boolean,
    isCompact: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val circleDp = if (isCompact) 10.dp else 13.dp
    val lineWidthDp = if (isCompact) 14.dp else 19.dp
    val lineHeightDp = if (isCompact) 2.5.dp else 3.dp
    val borderDp = if (isCompact) 2.5.dp else 3.dp

    val totalWidth = lineWidthDp + circleDp
    val totalHeight = circleDp

    Canvas(modifier = modifier.size(width = totalWidth, height = totalHeight)) {
        val circlePx = circleDp.toPx()
        val lineWidthPx = lineWidthDp.toPx()
        val lineHeightPx = lineHeightDp.toPx()
        val borderPx = borderDp.toPx()

        val cy = size.height / 2f
        val r = circlePx / 2f

        if (isChecked) {
            drawRect(
                color = color,
                topLeft = Offset(0f, cy - lineHeightPx / 2f),
                size = Size(lineWidthPx, lineHeightPx)
            )
            drawCircle(
                color = color,
                radius = r,
                center = Offset(lineWidthPx + r, cy)
            )
        } else {
            drawCircle(
                color = color,
                radius = r - borderPx / 2f,
                center = Offset(r, cy),
                style = Stroke(width = borderPx)
            )
            drawRect(
                color = color,
                topLeft = Offset(circlePx, cy - lineHeightPx / 2f),
                size = Size(lineWidthPx, lineHeightPx)
            )
        }
    }
}

class LightToggle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private val mark = ToggleMark(context)
    private val label = TextView(context)
    private var listener: ((Boolean) -> Unit)? = null

    var isChecked: Boolean
        get() = mark.checked
        set(value) { mark.checked = value }

    var isCompact: Boolean = false
        set(value) {
            field = value
            updateStyles()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        isClickable = true
        isFocusable = true

        val defaultColor = resolvePrimaryTextColor(context)

        addView(mark)
        label.apply {
            setTextColor(defaultColor)
            attrs?.let {
                val a = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.text))
                if (a.hasValue(0)) {
                    text = a.getText(0)
                }
                a.recycle()
            }
        }
        addView(label, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))

        mark.setMarkColor(defaultColor)
        updateStyles()

        setOnClickListener {
            mark.checked = !mark.checked
            listener?.invoke(mark.checked)
        }
    }

    private fun resolvePrimaryTextColor(context: Context): Int {
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)) {
            return typedValue.data
        }
        return context.getColor(R.color.white)
    }

    private fun updateStyles() {
        val density = resources.displayMetrics.density
        minimumHeight = ((if (isCompact) 44f else 48f) * density).toInt()
        
        val markParams = mark.layoutParams as? LayoutParams ?: LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        markParams.marginEnd = ((if (isCompact) 12f else 18f) * density).toInt()
        mark.layoutParams = markParams

        label.textSize = if (isCompact) 16f else 20f
        mark.isCompact = isCompact
    }

    fun setText(text: CharSequence) { label.text = text }

    fun setToggleColor(color: Int) {
        label.setTextColor(color)
        mark.setMarkColor(color)
    }

    fun setOnCheckedChangeListener(onChange: (Boolean) -> Unit) { listener = onChange }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else 0.4f
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = "android.widget.Switch"
        info.isCheckable = true
        info.isChecked = isChecked
    }

    private class ToggleMark(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private var circle = 13f * density
        private var lineWidth = 19f * density
        private var lineHeight = 3f * density
        private var border = 3f * density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.white)
        }

        fun setMarkColor(color: Int) {
            paint.color = color
            invalidate()
        }

        var isCompact = false
            set(value) {
                field = value
                circle = (if (value) 10f else 13f) * density
                lineWidth = (if (value) 14f else 19f) * density
                lineHeight = (if (value) 2.5f else 3f) * density
                border = (if (value) 2.5f else 3f) * density
                requestLayout()
                invalidate()
            }

        var checked = false
            set(value) {
                if (field == value) return
                field = value
                invalidate()
            }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension((lineWidth + circle).toInt(), circle.toInt())
        }

        override fun onDraw(canvas: Canvas) {
            val cy = height / 2f
            val r = circle / 2f
            if (checked) {
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, cy - lineHeight / 2, lineWidth, cy + lineHeight / 2, paint)
                canvas.drawCircle(lineWidth + r, cy, r, paint)
            } else {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = border
                canvas.drawCircle(r, cy, r - border / 2, paint)
                paint.style = Paint.Style.FILL
                canvas.drawRect(circle, cy - lineHeight / 2, circle + lineWidth, cy + lineHeight / 2, paint)
            }
        }
    }
}
