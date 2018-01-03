package com.squareup.okhttptestapp.spec

import com.facebook.litho.annotations.FromEvent
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.sections.Children
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.annotations.GroupSectionSpec
import com.facebook.litho.sections.annotations.OnCreateChildren
import com.facebook.litho.sections.common.DataDiffSection
import com.facebook.litho.sections.common.OnCheckIsSameItemEvent
import com.facebook.litho.sections.common.RenderEvent
import com.facebook.litho.sections.common.SingleComponentSection
import com.facebook.litho.widget.ComponentRenderInfo
import com.facebook.litho.widget.RenderInfo
import com.squareup.okhttptestapp.model.ResponseModel

@GroupSectionSpec
object ResultsListSectionSpec {
  @OnCreateChildren
  fun onCreateChildren(c: SectionContext, @Prop results: List<ResponseModel>): Children {
    return Children.create()
        .child(
            DataDiffSection.create<ResponseModel>(c)
                .data(results)
                .renderEventHandler(
                    ResultsListSection.render(c))
                .onCheckIsSameItemEventHandler(
                    ResultsListSection.isSameItem(c)))
        .build()
  }

  @OnEvent(RenderEvent::class)
  fun render(
      c: SectionContext,
      @FromEvent model: ResponseModel): RenderInfo =
      ComponentRenderInfo.create()
          .component(ResultComponent.create(c).text(model.body).build())
          .build()

  @OnEvent(OnCheckIsSameItemEvent::class)
  fun isSameItem(
      c: SectionContext,
      @FromEvent previousItem: ResponseModel,
      @FromEvent nextItem: ResponseModel): Boolean = previousItem.requestNum == nextItem.requestNum
}