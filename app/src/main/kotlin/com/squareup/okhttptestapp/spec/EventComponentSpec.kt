package com.squareup.okhttptestapp.spec

import com.facebook.litho.ClickEvent
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateInitialState
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.OnUpdateState
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.makeramen.litho.children
import com.makeramen.litho.column
import com.makeramen.litho.layout
import com.makeramen.litho.text
import com.squareup.okhttptestapp.model.AppEvent

@LayoutSpec
object EventComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext,
      expanded: StateValue<Boolean>) {
    expanded.set(false)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @Prop result: AppEvent, @State expanded: Boolean): ComponentLayout =
      layout {
        column(c) {
          children {
            text(c) {
              text(result.display(expanded))
              textSizeSp(12f)
              clickHandler(EventComponent.onClick(c))
            }
          }
        }
      }

  @OnUpdateState
  fun updateExpanded(expanded: StateValue<Boolean>) {
    expanded.set(!expanded.get())
  }

  @OnEvent(ClickEvent::class)
  fun onClick(c: ComponentContext) {
    EventComponent.updateExpandedAsync(c);
  }
}