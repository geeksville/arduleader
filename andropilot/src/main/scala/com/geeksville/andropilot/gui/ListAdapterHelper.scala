package com.geeksville.andropilot.gui

import android.support.v4.app.ListFragment
import com.ridemission.scandroid.AndroidLogger
import scala.collection.JavaConverters._
import android.widget.SimpleAdapter
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import java.util.ArrayList

/**
 * A mixin that makes it easier to use SimpleAdapters
 */
abstract class ListAdapterHelper[T] extends ListFragment with AndroidLogger {
  protected def fromKeys: Array[String]
  protected def toFields: Array[Int]
  protected def makeRow(i: Int, p: T): Map[String, Any]
  protected def isSelected(row: Int): Boolean
  protected def rowId: Int

  private var adapter: Option[SimpleAdapter] = None
  private var adapterList: Option[java.util.List[java.util.Map[String, Any]]] = None
  private var oldSrc: Option[Seq[T]] = None

  protected def updateAdapter(newsrc: Seq[T], i: Int) {
    if (!adapterList.isDefined || oldSrc.map(_.size).getOrElse(-1) != newsrc.size || newsrc != oldSrc.get) {
      debug("Size is wrong, must recreate")
      setAdapter(newsrc);
    } else {
      debug("Invaliding due to update: " + i + " " + newsrc(i))
      adapterList.get.set(i, makeRow(i, newsrc(i)).asJava)
      adapter.get.notifyDataSetChanged()
    }
  }

  protected def setAdapter(src: Seq[T]) {
    debug("Setting list to " + src.size + " items")
    oldSrc = Some(src)

    val asMap = src.zipWithIndex.map {
      case (p, i) =>
        makeRow(i, p).asJava
    }

    // If we already have an adapter, just refill it
    adapter match {
      case None =>
        // Make a new adapter - but make sure java creates a _modifable_ list
        val asJavaMap = new ArrayList[java.util.Map[String, Any]](asMap.size)
        asMap.foreach(asJavaMap.add)

        adapterList = Some(asJavaMap)
        adapter = Some(new SimpleAdapter(getActivity, asJavaMap, rowId, fromKeys, toFields) {

          // Show selected item in a color
          override def getView(position: Int, convertView: View, parent: ViewGroup) = {
            val itemView = super.getView(position, convertView, parent)
            //debug("in getView " + position)
            if (isSelected(position)) {
              //debug("Selecting " + itemView)
              itemView.setBackgroundColor(Color.LTGRAY)
            } else
              itemView.setBackgroundColor(Color.TRANSPARENT)
            itemView
          }
        })

        // Give our adapter to Android
        setListAdapter(adapter.get)
      case Some(a) =>
        adapterList.foreach { l =>
          l.clear()
          asMap.foreach(l.add)
          a.notifyDataSetChanged()
        }
    }
  }
}