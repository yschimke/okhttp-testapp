package com.squareup.okhttptestapp.spec

import android.graphics.Color
import com.facebook.litho.Border
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
import com.facebook.yoga.YogaEdge
import com.squareup.okhttptestapp.model.AppEvent
import com.squareup.okhttptestapp.model.ClientCreated
import com.squareup.okhttptestapp.model.GmsInstall
import com.squareup.okhttptestapp.model.NetworkEvent
import com.squareup.okhttptestapp.model.PlatformEvent
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
                .renderEventHandler(ResultsListSection.render(c))
                .onCheckIsSameItemEventHandler(ResultsListSection.isSameItem(c)))
        .build()
  }

  @OnEvent(RenderEvent::class)
  fun render(
      c: SectionContext,
      @FromEvent model: AppEvent,
      @FromEvent index: Int): RenderInfo {
    val component = when (model) {
      is ResponseModel -> ResultComponent.create(c).result(model)
      is GmsInstall -> textRow(c, "GMS Provider: ${model.error ?: "installed"}")
      is SystemState -> textRow(c, "System State: ${model.state}")
      is ClientCreated -> textRow(c, "Client: ${model.description}")
      is NetworkEvent -> textRow(c, "Network: ${model.description}")
      is PlatformEvent -> textRow(c, "AndroidOptimizedPlatform: ${model.error ?: " available"}")
    }

    if (index > 0) {
      component.border(Border.create(c).color(YogaEdge.TOP, Color.GRAY).widthDip(YogaEdge.TOP,
          1).build())
    }

    component.marginDip(YogaEdge.ALL, 5f)

    return ComponentRenderInfo.create().component(component.build()).build()
  }

  private fun textRow(c: SectionContext, text: String) = Text.create(c).text(text).textSizeSp(
      12f)

  @OnEvent(OnCheckIsSameItemEvent::class)
  fun isSameItem(
      c: SectionContext,
      @FromEvent previousItem: AppEvent,
      @FromEvent nextItem: AppEvent): Boolean {
    return previousItem == nextItem
  }
}