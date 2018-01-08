package com.squareup.okhttptestapp.spec

import android.widget.Button
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.MountSpec
import com.facebook.litho.annotations.OnCreateMountContent
import com.facebook.litho.annotations.OnMount
import com.facebook.litho.annotations.Prop

@MountSpec
object ButtonComponentSpec {
  @OnCreateMountContent
  fun onCreateMountContent(c: ComponentContext): Button {
    return Button(c)
  }

  @OnMount
  fun onMount(
      c: ComponentContext,
      button: Button,
      @Prop label: String,
      @Prop executeListener: () -> Unit) {
    button.text = label
    button.setOnClickListener { executeListener() }
  }
}