package com.squareup.okhttptestapp.spec

import android.text.InputType
import android.util.Log
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
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
import com.makeramen.litho.component
import com.makeramen.litho.editText
import com.makeramen.litho.layout
import com.makeramen.litho.row
import com.squareup.okhttptestapp.RequestOptions

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
      c: ComponentContext, @State requestOptions: RequestOptions, @Prop executeListener: (RequestOptions) -> Unit): ComponentLayout =
      layout {
        row(c) {
          children {
            editText(c) {
              text(requestOptions.url)
              textSizeSp(14f)
              isSingleLine(true)
              inputType(InputType.TYPE_TEXT_VARIATION_URI).flexGrow(1f)
              textChangedEventHandler(QueryComponent.onUrlChanged(c))
            }
            component(c, ButtonComponent::create) {
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

  @OnUpdateState
  fun updateTextValue(requestOptions: StateValue<RequestOptions>, @Param updatedText: String) {
    Log.i("QueryComponentSpec", "$updatedText")
    val newRequestOptions = requestOptions.get().copy(url = updatedText)
    requestOptions.set(newRequestOptions)
  }

  @OnEvent(TextChangedEvent::class)
  fun onUrlChanged(
      c: ComponentContext,
      @FromEvent text: String) {
    QueryComponent.updateTextValue(c, text)
  }
}