package com.squareup.okhttptestapp.spec

import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionComponent
import com.facebook.yoga.YogaEdge
import com.makeramen.litho.children
import com.makeramen.litho.column
import com.makeramen.litho.component
import com.makeramen.litho.layout
import com.squareup.okhttptestapp.model.AppEvent
import com.squareup.okhttptestapp.model.RequestOptions

@LayoutSpec
object MainComponentSpec {
  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext,
      @Prop requestOptions: RequestOptions,
      @Prop executeListener: (RequestOptions) -> Unit,
      @Prop results: List<AppEvent>): ComponentLayout = layout {
    column(c) {
      paddingDip(YogaEdge.ALL, 8f)
      children {
        component(c, QueryComponent::create) {
          initialRequestOptions(requestOptions)
          executeListener(executeListener)
          flexGrow(0f)
        }
        component(c, RecyclerCollectionComponent::create) {
          section(ResultsListSection.create(SectionContext(c)).results(results)).disablePTR(
              true).flexGrow(1f)
        }
      }
    }
  }
}