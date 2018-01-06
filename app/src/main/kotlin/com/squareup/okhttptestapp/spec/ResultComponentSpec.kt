package com.squareup.okhttptestapp.spec

import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.squareup.okhttptestapp.model.ResponseModel
import okhttp3.Response

@LayoutSpec
object ResultComponentSpec {
  @OnCreateLayout
  fun onCreateLayout(c: ComponentContext, @Prop result: ResponseModel): ComponentLayout {
    val text = resultText(result)
    return Column.create(c)
        .child(Text.create(c).text(text).textSizeSp(12f).build())
        .build()
  }

  private fun resultText(result: ResponseModel) =
      if (result.response!!.isSuccessful) result.body else ("${result.response.code()}: ${result.response.message()}")
}