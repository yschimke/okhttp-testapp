package com.squareup.okhttptestapp.spec

import android.text.InputType
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateInitialState
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.facebook.litho.widget.EditText
import okhttp3.Request

@LayoutSpec
object QueryComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext, url: StateValue<String>, @Prop initialUrl: String) {
    url.set(initialUrl)
  }

  @JvmStatic
  @OnCreateLayout
  fun onCreateLayout(c: ComponentContext, @State url: String): ComponentLayout {
    return Column.create(c)
        .child(EditText.create(c)
            .text(url)
            .textSizeSp(14f)
            .isSingleLine(true)
            .inputType(InputType.TYPE_TEXT_VARIATION_URI)
            .clickHandler(QueryComponent.onClick(c)))
        .build()
  }

  @JvmStatic
  @OnEvent(ClickEvent::class)
  fun onClick(c: ComponentContext, @State url: String, @Prop executeListener: (Request) -> Unit) {
    var request = Request.Builder().url(url).build()

    executeListener(request)
  }

}