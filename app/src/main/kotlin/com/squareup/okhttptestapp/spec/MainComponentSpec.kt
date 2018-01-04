package com.squareup.okhttptestapp.spec

import android.graphics.Color
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionComponent
import com.facebook.yoga.YogaEdge
import com.squareup.okhttptestapp.model.ResponseModel
import okhttp3.Request

@LayoutSpec
object MainComponentSpec {
  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @Prop initialUrl: String, @Prop executeListener: (Request) -> Unit,
      @Prop results: List<ResponseModel>): ComponentLayout {
    println(results)

    val list = RecyclerCollectionComponent
        .create(c)
        .section(
            ResultsListSection.create(SectionContext(c)).results(results).build())
        .disablePTR(true)
        .build()

    return Column.create(c)
        .paddingDip(YogaEdge.ALL, 16f)
        .backgroundColor(Color.WHITE)
        .child(QueryComponent.create(c).initialUrl(initialUrl).executeListener(
            executeListener).build())
        .child(list)
        .build()
  }
}