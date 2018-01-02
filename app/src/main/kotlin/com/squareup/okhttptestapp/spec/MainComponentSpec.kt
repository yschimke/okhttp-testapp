package com.squareup.okhttptestapp.spec

import android.graphics.Color
import android.text.InputType
import android.util.Log
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.TextChangedEvent
import com.facebook.yoga.YogaEdge
import okhttp3.Request

@FunctionalInterface
interface OnExecuteListener {
  fun onExecute(request: Request): Unit
}

@LayoutSpec
class MainComponentSpec {
  companion object {
    @JvmStatic
    @OnCreateInitialState
    fun createInitialState(c: ComponentContext, url: StateValue<String>, @Prop initialUrl: String) {
      url.set(initialUrl)
    }

    @JvmStatic
    @OnCreateLayout
    fun onCreateLayout(
        c: ComponentContext, @State url: String, @Prop recyclerBinder: RecyclerBinder): ComponentLayout {
      return Column.create(c)
          .paddingDip(YogaEdge.ALL, 16f)
          .backgroundColor(Color.WHITE)
          .child(urlText(c, url))
          .child(getRecyclerComponent(c, recyclerBinder))
          .build()
    }

//    @JvmStatic
//    @OnEvent(TextChangedEvent::class)
//    fun onTextChanged(c: ComponentContext, @State url: String) {
//      Log.i("X", "tc: " + url)
//    }

    @JvmStatic
    @OnEvent(ClickEvent::class)
    fun onClick(c: ComponentContext, @State url: String, @Prop executeListener: (Request) -> Unit) {
      var request = Request.Builder().url(url).build()

      executeListener(request)
    }

    private fun urlText(c: ComponentContext, url: String): EditText.Builder? {
      return EditText.create(c)
          .text(url)
          .textSizeSp(14f)
          .isSingleLine(true)
          .inputType(InputType.TYPE_TEXT_VARIATION_URI)
          .clickHandler(MainComponent.onClick(c))
//          .textChangedEventHandler(MainComponent.onTextChanged(c))
    }

    private fun getRecyclerComponent(c: ComponentContext,
        recyclerBinder: RecyclerBinder): Component<Recycler> {
      return Recycler.create(c).binder(recyclerBinder).build()
    }
  }
}