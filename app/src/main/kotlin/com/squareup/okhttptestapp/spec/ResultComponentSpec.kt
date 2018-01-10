package com.squareup.okhttptestapp.spec

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
import com.makeramen.litho.children
import com.makeramen.litho.column
import com.makeramen.litho.layout
import com.makeramen.litho.text
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
        ResultComponent.updateResponseAsync(c, e.toString())
      }

      override fun onResponse(call: Call, response: Response) {
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
      c: ComponentContext, @Prop result: ResponseModel, @State responseBody: String): ComponentLayout =
      layout {
        column(c) {
          children {
            text(c) {
              text(resultText(result, responseBody))
              textSizeSp(12f)
            }
          }
        }
      }

  private fun resultText(result: ResponseModel,
      responseBody: String) =
      "" + result.call.request().url() + "\n" + responseBody
}