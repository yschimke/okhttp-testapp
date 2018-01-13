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
import com.facebook.litho.widget.ComponentRenderInfo
import com.facebook.litho.widget.RenderInfo
import com.facebook.litho.widget.Text
import com.squareup.okhttptestapp.model.AppEvent
import com.squareup.okhttptestapp.model.GmsInstall
import com.squareup.okhttptestapp.model.ResponseModel
import com.squareup.okhttptestapp.model.SystemState

@GroupSectionSpec
object ResultsListSectionSpec {
  @OnCreateChildren
  fun onCreateChildren(c: SectionContext, @Prop results: List<AppEvent>): Children {
    return Children.create()
        .child(
            DataDiffSection.create<AppEvent>(c)
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
      @FromEvent model: AppEvent): RenderInfo {
    val component = when (model) {
      is ResponseModel -> ResultComponent.create(c).result(model).build()
      is GmsInstall -> textRow(c, "GMS Provider Installed")
      is SystemState -> textRow(c, "System State: ${model.state}")
    }

    return ComponentRenderInfo.create().component(component).build()
  }

  private fun textRow(c: SectionContext, text: String) = Text.create(c).text(text).textSizeSp(
      12f).build()

  @OnEvent(OnCheckIsSameItemEvent::class)
  fun isSameItem(
      c: SectionContext,
      @FromEvent previousItem: AppEvent,
      @FromEvent nextItem: AppEvent): Boolean {
    return previousItem == nextItem
  }
}