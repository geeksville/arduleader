package com.geeksville.andropilot.gui

import android.content.Context
import android.util.AttributeSet
import android.graphics._
import android.view._
import com.ridemission.scandroid.AndroidLogger

class JoystickView(context: Context, attrs: AttributeSet) extends View(context, attrs) with AndroidLogger {

  // center coords
  var cX = 0
  var cY = 0

  var touchX = 0
  var touchY = 0

  var ctrlRadius = 0
  var handleRadius = 0
  var movementRadius = 0

  var pointerId: Option[Int] = None

  val bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
    setColor(Color.DKGRAY)
    setStrokeWidth(1)
    setStyle(Paint.Style.FILL_AND_STROKE)
  }

  val handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
    setColor(Color.LTGRAY)
    setStrokeWidth(1)
    setStyle(Paint.Style.FILL_AND_STROKE)
  }

  val selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
    setColor(Color.YELLOW)
    setStrokeWidth(1)
    setStyle(Paint.Style.FILL_AND_STROKE)
  }

  // We want clicks
  setClickable(true)

  override def onDraw(canvas: Canvas) {
    canvas.save()
    // Draw the background
    val rect = new RectF(0, 0, getMeasuredWidth, getMeasuredHeight)
    canvas.drawRoundRect(rect, handleRadius, handleRadius, bgPaint)

    // Draw the handle
    val handleX = touchX + cX
    val handleY = touchY + cY
    canvas.drawCircle(handleX, handleY, handleRadius, if (pointerId.isDefined) selectedPaint else handlePaint)

    canvas.restore()
  }

  override def onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    cX = getMeasuredWidth() / 2
    cY = getMeasuredHeight() / 2

    val shortestSide = math.min(getMeasuredWidth, getMeasuredHeight)
    val pad = 4
    ctrlRadius = shortestSide / 2 - pad
    handleRadius = (shortestSide * 0.075).toInt

    movementRadius = ctrlRadius - handleRadius
  }

  def onUserRelease() {
    pointerId.foreach { id =>
      pointerId = None
      invalidate()
    }
  }

  override def onTouchEvent(ev: MotionEvent) = {
    val action = ev.getAction
    debug("Got action " + action)
    (action & MotionEvent.ACTION_MASK) match {
      case MotionEvent.ACTION_MOVE =>
        processMoveEvent(ev)
        true

      case MotionEvent.ACTION_CANCEL =>
        onUserRelease()
        true

      case MotionEvent.ACTION_UP =>
        onUserRelease()
        true

      case MotionEvent.ACTION_POINTER_UP =>
        val pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val newId = ev.getPointerId(pointerIndex)
        if (pointerId == Some(newId))
          onUserRelease()
        true

      case MotionEvent.ACTION_DOWN =>
        if (!pointerId.isDefined)
          pointerId = Some(ev.getPointerId(0))
        true

      case MotionEvent.ACTION_POINTER_DOWN =>
        if (!pointerId.isDefined) {
          val pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT
          val newId = ev.getPointerId(pointerIndex)
          pointerId = Some(newId)
        }
        true

      case _ =>
        false // Not for us
    }
  }

  private def clampRadius(delta: Int) =
    if (delta > movementRadius)
      movementRadius
    else if (delta < -movementRadius)
      -movementRadius
    else
      delta

  def processMoveEvent(ev: MotionEvent) {
    pointerId.foreach { id =>
      val pointerIndex = ev.findPointerIndex(id)

      // Translate touch position to center of view
      val x = ev.getX(pointerIndex).toInt - cX
      touchX = clampRadius(x)
      val y = ev.getY(pointerIndex).toInt - cY
      touchY = clampRadius(y)

      invalidate()
    }
  }

}