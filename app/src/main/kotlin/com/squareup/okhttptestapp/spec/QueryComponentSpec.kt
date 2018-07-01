package com.squareup.okhttptestapp.spec

import android.text.InputType
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.FromEvent
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateInitialState
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.OnUpdateState
import com.facebook.litho.annotations.Param
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.facebook.litho.widget.TextChangedEvent
import com.makeramen.litho.children
import com.makeramen.litho.column
import com.makeramen.litho.componentBuilder
import com.makeramen.litho.editText
import com.makeramen.litho.row
import com.squareup.okhttptestapp.model.RequestOptions

@LayoutSpec
object QueryComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext,
      requestOptions: StateValue<RequestOptions>,
      @Prop initialRequestOptions: RequestOptions) {
    requestOptions.set(initialRequestOptions)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @State requestOptions: RequestOptions, @Prop executeListener: (RequestOptions) -> Unit): Component =
        column(c) {
          children {
            row(c) {
              children {
                editText(c) {
                  text(requestOptions.url)
                  textSizeSp(14f)
                  isSingleLine(true)
                  inputType(InputType.TYPE_TEXT_VARIATION_URI).flexGrow(1f)
                  textChangedEventHandler(QueryComponent.onUrlChanged(c))
                }
              }
            }
            row(c) {
              children {
                componentBuilder(c, ButtonComponent::create) {
                  label("Fetch")
                  widthDip(100f)
                  heightDip(40f)
                  executeListener {
                    executeListener(requestOptions)
                  }
                }
              }
            }
          }
      }.build()

  @OnUpdateState
  fun updateUrl(requestOptions: StateValue<RequestOptions>, @Param updatedText: String) {
    requestOptions.set(requestOptions.get().copy(url = updatedText))
  }

  @OnEvent(TextChangedEvent::class)
  fun onUrlChanged(
      c: ComponentContext,
      @FromEvent text: String) {
    QueryComponent.updateUrl(c, text)
  }
}