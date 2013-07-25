package com.geeksville.andropilot.gui

import android.view.View
import android.content.Context
import android.util.AttributeSet
import android.graphics._

class JoystickView(context: Context, attrs: AttributeSet) extends View(context, attrs) {

  // center coords
  var cX = 0
  var cY = 0

  var touchX = 0
  var touchY = 0

  var ctrlRadius = 0
  var handleRadius = 0
  var movementRadius = 0

  val bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
    setColor(Color.GRAY)
    setStrokeWidth(1)
    setStyle(Paint.Style.FILL_AND_STROKE)
  }

  val handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
    setColor(Color.DKGRAY);
    setStrokeWidth(1);
    setStyle(Paint.Style.FILL_AND_STROKE)
  }

  override def onDraw(canvas: Canvas) {
    canvas.save()
    // Draw the background
    canvas.drawCircle(cX, cY, ctrlRadius, bgPaint);

    // Draw the handle
    val handleX = touchX + cX;
    val handleY = touchY + cY;
    canvas.drawCircle(handleX, handleY, handleRadius, handlePaint);

    canvas.restore()
  }

  override def onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom);

    cX = getMeasuredWidth() / 2;
    cY = getMeasuredHeight() / 2;

    val shortestSide = math.min(getMeasuredWidth, getMeasuredHeight)
    val pad = 4
    ctrlRadius = shortestSide / 2 - pad
    handleRadius = (shortestSide * 0.1).toInt
    movementRadius = math.min(cX, cY) - handleRadius
  }

}