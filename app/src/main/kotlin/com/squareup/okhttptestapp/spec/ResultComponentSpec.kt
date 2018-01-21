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
import com.facebook.litho.annotations.Param
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.makeramen.litho.children
import com.makeramen.litho.column
import com.makeramen.litho.layout
import com.makeramen.litho.text
import com.squareup.okhttptestapp.model.CallEvent
import com.squareup.okhttptestapp.model.CompletedResponse
import com.squareup.okhttptestapp.model.FailedResponse
import com.squareup.okhttptestapp.model.InProgress
import com.squareup.okhttptestapp.model.ResponseModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

@LayoutSpec
object ResultComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext,
      response: StateValue<ResponseModel>,
      @Prop result: CallEvent,
      expanded: StateValue<Boolean>) {
    expanded.set(false)
    response.set(InProgress())

    if (result.call.isExecuted) {
      response.set(FailedResponse(IOException("already executed")))
    } else {
      response.set(InProgress())

      result.call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          ResultComponent.updateResponseAsync(c, FailedResponse(e))
        }

        override fun onResponse(call: Call, httpResponse: Response) {
          val body = httpResponse.body()?.string()
          ResultComponent.updateResponseAsync(c, CompletedResponse(httpResponse, httpResponse.code(), body))
        }
      })
    }
  }

  @OnUpdateState
  fun updateResponse(response: StateValue<ResponseModel>, @Param newResult: ResponseModel) {
    response.set(newResult)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext, @Prop result: CallEvent, @State response: ResponseModel, @State expanded: Boolean): ComponentLayout =
      layout {
        column(c) {
          children {
            text(c) {
              text(resultText(result, response, expanded))
              textSizeSp(12f)
              clickHandler(ResultComponent.onClick(c))
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
    ResultComponent.updateExpandedAsync(c);
  }

  private fun resultText(result: CallEvent, response: ResponseModel, expanded: Boolean): String {
    val status = when (response) {
      is InProgress -> "..."
      is FailedResponse -> response.exception.message
      is CompletedResponse -> response.code
    }
    val mainLine = "${result.call.request().url()} $status"

    if (expanded) {
      val full = when (response) {
        is InProgress -> null
        is FailedResponse -> stackTrace(response.exception)
        is CompletedResponse -> response.bodyText
      }

      if (full != null) {
        return "$mainLine\n$full"
      }
    }

    return mainLine
  }

  private fun stackTrace(exception: IOException): String = StringWriter().use {
    exception.printStackTrace(PrintWriter(it))
    it.toString()
  }
}