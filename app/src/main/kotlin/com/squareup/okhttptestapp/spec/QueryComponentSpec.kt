package com.squareup.okhttptestapp.spec

import android.text.InputType
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
import com.makeramen.litho.column
import com.makeramen.litho.component
import com.makeramen.litho.editText
import com.makeramen.litho.layout
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
      c: ComponentContext, @State requestOptions: RequestOptions, @Prop gmsAvailable: Boolean, @Prop executeListener: (RequestOptions) -> Unit): ComponentLayout =
      layout {
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
                component(c, CheckboxComponent::create) {
                  label("GMS")
                  checked(gmsAvailable && requestOptions.gms)
                  widthDip(80f)
                  heightDip(40f)
                  available(gmsAvailable)
                  checkedListener {
                    QueryComponent.updateGcm(c, it)
                  }
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
        }
      }

  @OnUpdateState
  fun updateUrl(requestOptions: StateValue<RequestOptions>, @Param updatedText: String) {
    requestOptions.set(requestOptions.get().copy(url = updatedText))
  }

  @OnUpdateState
  fun updateGcm(requestOptions: StateValue<RequestOptions>, @Param updatedGms: Boolean) {
    requestOptions.set(requestOptions.get().copy(gms = updatedGms))
  }

  @OnEvent(TextChangedEvent::class)
  fun onUrlChanged(
      c: ComponentContext,
      @FromEvent text: String) {
    QueryComponent.updateUrl(c, text)
  }
}