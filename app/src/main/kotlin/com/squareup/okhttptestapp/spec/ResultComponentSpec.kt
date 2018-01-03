package com.squareup.okhttptestapp.spec

import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text

@LayoutSpec
class ResultComponentSpec {
  companion object {
    @JvmStatic
    @OnCreateLayout
    fun onCreateLayout(c: ComponentContext, @Prop text: String): ComponentLayout {
      return Column.create(c)
          .child(Text.create(c).text(text).textSizeSp(12f).build())
          .build()
    }
  }
}