package com.innerdrawer.Helpers

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

abstract class OnSwipeTouchListener(context: Context?) : View.OnTouchListener {
    private val gestureDetector: GestureDetector
    private var view: View? = null
    private  val SWIPE_THRESHOLD = 100
    private  val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        this.view = view
        return gestureDetector.onTouchEvent(event)
    }

    abstract fun onSwipeRight(view: View?)
    abstract fun onSwipeLeft(view: View?)
    abstract fun onSwipeBottom(view: View?)
    abstract fun onSwipeTop(view: View?)
    abstract fun onClick(view: View?)
    abstract fun onLongClick(view: View?): Boolean
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick(view)
            super.onLongPress(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(view)
            return super.onSingleTapUp(e)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY: Float = e2.getY() - e1.getY()
                val diffX: Float = e2.getX() - e1.getX()
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(view)
                        } else {
                            onSwipeLeft(view)
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom(view)
                    } else {
                        onSwipeTop(view)
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }

    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}