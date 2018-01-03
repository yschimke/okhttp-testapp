package com.squareup.okhttptestapp.spec

import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.yoga.YogaEdge

@LayoutSpec
class ResultsListComponentSpec {
  companion object {
    @JvmStatic
    @OnCreateLayout
    fun onCreateLayout(
        c: ComponentContext, @Prop recyclerBinder: RecyclerBinder): ComponentLayout {
      return Column.create(c)
          .paddingDip(YogaEdge.ALL, 16f)
          .backgroundColor(Color.WHITE)
          .child(Recycler.create(c).binder(recyclerBinder).build())
          .build()
    }
  }
}