package com.squareup.okhttptestapp.spec

import android.util.Log
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateInitialState
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnUpdateState
import com.facebook.litho.annotations.Param
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.facebook.litho.widget.Text
import com.squareup.okhttptestapp.model.ResponseModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

@LayoutSpec
object ResultComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext,
      responseBody: StateValue<String>, @Prop result: ResponseModel) {
    responseBody.set("Executing...")

    result.call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        Log.i("ResultComponent", "Request failed", e)
        ResultComponent.updateResponseAsync(c, e.toString())
      }

      override fun onResponse(call: Call, response: Response) {
        Log.i("ResultComponent", "s")
        ResultComponent.updateResponseAsync(c, response.body()!!.string())
      }
    })
  }

  @OnUpdateState
  fun updateResponse(responseBody: StateValue<String>, @Param newResult: String) {
    responseBody.set(newResult)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @Prop result: ResponseModel, @State responseBody: String): ComponentLayout {
    val text = resultText(result, responseBody)
    return Column.create(c)
        .child(Text.create(c).text(text).textSizeSp(12f).build())
        .build()
  }

  private fun resultText(result: ResponseModel,
      responseBody: String) =
      "" + result.call.request().url() + "\n" + responseBody
}