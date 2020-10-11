package com.example.fancontroller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

/*
 * Custom View for our controller.
 */
class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0

    // Radius of the circle.
    private var radius = 0.0f

    // The active selection.
    private var fanSpeed = FanSpeed.OFF

    // Position variable which will be used to draw label and indicator circle position.
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)
        // Forces a draw or redraw of the view.
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set dial background color according to fan's speed.
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor
        }
        // Draw the dial.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        // Draw the fan.
        val fan = BitmapFactory.decodeResource(context.resources, R.drawable.fan)
        val rectangle = Rect(150, 150, 500, 500)
        canvas.drawBitmap(fan, null, rectangle, null)
        // Draw the indicator circle.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)
        // Draw the text labels.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
        // Draw animated propeller depending on speed.
        when (fanSpeed) {
            FanSpeed.LOW -> canvas.drawBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.low_speed
                ), null, Rect(0, 0, 175, 175), null
            )
            FanSpeed.MEDIUM -> canvas.drawBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.medium_speed
                ), null, Rect(0, 0, 175, 175), null
            )
            FanSpeed.HIGH -> canvas.drawBitmap(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.max_speed
                ), null, Rect(0, 0, 175, 175), null
            )
            else -> FanSpeed.OFF
        }
    }
}