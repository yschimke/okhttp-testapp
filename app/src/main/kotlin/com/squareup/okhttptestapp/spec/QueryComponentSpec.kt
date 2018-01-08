package com.squareup.okhttptestapp.spec

import android.text.InputType
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.Row
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
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.TextChangedEvent
import okhttp3.Request

@LayoutSpec
object QueryComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext, url: StateValue<String>, @Prop initialUrl: String) {
    url.set(initialUrl)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @State url: String, @Prop executeListener: (Request) -> Unit): ComponentLayout {
    return Row.create(c)
        .child(EditText.create(c)
            .text(url)
            .textSizeSp(14f)
            .isSingleLine(true)
            .inputType(InputType.TYPE_TEXT_VARIATION_URI).flexGrow(1f)
            .textChangedEventHandler(QueryComponent.onUrlChanged(c)))
        .child(
            ButtonComponent.create(c).label("Fetch").widthDip(100f).heightDip(40f).executeListener {
              executeListener(Request.Builder().url(url).build())
            }.flexGrow(0f))
        .build()
  }

  @OnUpdateState
  fun updateTextValue(url: StateValue<String>, @Param updatedUrl: String) {
    url.set(updatedUrl)
  }

  @OnEvent(TextChangedEvent::class)
  fun onUrlChanged(
      c: ComponentContext,
      @FromEvent text: String) {
    QueryComponent.updateTextValueAsync(c, text)
  }
}