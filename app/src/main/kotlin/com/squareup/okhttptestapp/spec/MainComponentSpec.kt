package com.squareup.okhttptestapp.spec

import android.graphics.Color
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.yoga.YogaEdge
import okhttp3.Request

@LayoutSpec
class MainComponentSpec {
  companion object {
    @JvmStatic
    @OnCreateLayout
    fun onCreateLayout(
        c: ComponentContext, @Prop initialUrl: String, @Prop recyclerBinder: RecyclerBinder, @Prop executeListener: (Request) -> Unit): ComponentLayout {
      return Column.create(c)
          .paddingDip(YogaEdge.ALL, 16f)
          .backgroundColor(Color.WHITE)
          .child(QueryComponent.create(c).initialUrl(initialUrl).executeListener(executeListener).build())
          .child(ResultsListComponent.create(c).recyclerBinder(recyclerBinder).build())
          .build()
    }
  }
}