package com.light.medication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView

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

        addView(mark)
        label.apply {
            setTextColor(context.getColor(R.color.white))
            attrs?.let {
                val a = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.text))
                text = a.getText(0)
                a.recycle()
            }
        }
        addView(label, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))

        updateStyles()

        setOnClickListener {
            mark.checked = !mark.checked
            listener?.invoke(mark.checked)
        }
    }

    private fun updateStyles() {
        val density = resources.displayMetrics.density
        minimumHeight = ((if (isCompact) 36f else 44f) * density).toInt()
        
        val markParams = mark.layoutParams as? LayoutParams ?: LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        markParams.marginEnd = ((if (isCompact) 12f else 18f) * density).toInt()
        mark.layoutParams = markParams

        label.textSize = if (isCompact) 16f else 20f
        mark.isCompact = isCompact
    }

    fun setText(text: CharSequence) { label.text = text }

    /** Fires only on user taps, never on programmatic [isChecked] changes. */
    fun setOnCheckedChangeListener(onChange: (Boolean) -> Unit) { listener = onChange }

    /** Dim and stop responding to taps while disabled (e.g. during the voice-model download). */
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

    /** The line-and-dot mark. Its width is constant across states so the row never shifts. */
    private class ToggleMark(context: Context) : View(context) {
        private val density = resources.displayMetrics.density
        private var circle = 13f * density
        private var lineWidth = 19f * density
        private var lineHeight = 3f * density
        private var border = 3f * density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.white)
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
                // line on the left, filled dot on the right
                paint.style = Paint.Style.FILL
                canvas.drawRect(0f, cy - lineHeight / 2, lineWidth, cy + lineHeight / 2, paint)
                canvas.drawCircle(lineWidth + r, cy, r, paint)
            } else {
                // hollow dot on the left, line on the right
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = border
                canvas.drawCircle(r, cy, r - border / 2, paint)
                paint.style = Paint.Style.FILL
                canvas.drawRect(circle, cy - lineHeight / 2, circle + lineWidth, cy + lineHeight / 2, paint)
            }
        }
    }
}
