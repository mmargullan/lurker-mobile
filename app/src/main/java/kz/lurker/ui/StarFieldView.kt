package kz.lurker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class StarFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Star(
        val x: Float,
        val y: Float,
        val size: Float,
        val flickerSpeed: Float,
        var phase: Float = Random.nextFloat() * 360f
    )

    private val stars = mutableListOf<Star>()
    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    init {
        post { generateStars() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateStars()
    }

    private fun generateStars(count: Int = 100) {
        stars.clear()
        for (i in 0 until count) {
            stars.add(
                Star(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    size = Random.nextFloat() * 3f + 1f,
                    flickerSpeed = Random.nextFloat() * 0.1f + 0.02f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (star in stars) {
            star.phase += star.flickerSpeed
            val alpha = ((sin(star.phase) + 1) / 2f * 255).toInt().coerceIn(0, 255)
            paint.alpha = alpha

            canvas.drawCircle(star.x, star.y, star.size, paint)
        }

        postInvalidateOnAnimation()
    }
}
