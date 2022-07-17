package com.dldmswo1209.recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var onRequestCurrentAplitude: (() -> Int)? = null

    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }
    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    private var drawingAmplitudes : List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition : Int = 0

    private val visualizeRepeatAction: Runnable = object : Runnable{
        override fun run() {
            if(!isReplaying) {
                // amplitude, Draw
                val currentAmplitude = onRequestCurrentAplitude?.invoke() ?: 0
                drawingAmplitudes = (listOf(currentAmplitude!!) + drawingAmplitudes)
            }else{
                replayingPosition++
            }
            invalidate() // 뷰 갱신

            handler?.postDelayed(this, ACTION_INTERVAL) // 20ms 후에 다시 실행
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val centerY = drawingHeight / 2f // 화면의 세로 기준 중앙
        var offsetX = drawingWidth.toFloat() // 시작 지점 화면의 오른쪽 끝

        drawingAmplitudes
            .let { amplitude ->
                if(isReplaying){
                    amplitude.takeLast(replayingPosition)
                }else{
                    amplitude
                }
            }
            .forEach { amplitude ->
                val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F // 음성의 진폭 계산

                offsetX -= LINE_SPACE
                if(offsetX < 0) return@forEach
                canvas.drawLine(
                    offsetX,
                    centerY - lineLength / 2F,
                    offsetX,
                    centerY + lineLength / 2F,
                    amplitudePaint
            )
        }
    }

    fun startVisualizing(isReplaying: Boolean){
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }
    fun stopVisualizing(){
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }
    fun clearVisualization(){
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object{
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE = 15F
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
        private const val ACTION_INTERVAL = 20L
    }
}