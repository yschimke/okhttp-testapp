package com.squareup.okhttptestapp.spec

import android.widget.CheckBox
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.MountSpec
import com.facebook.litho.annotations.OnCreateMountContent
import com.facebook.litho.annotations.OnMount
import com.facebook.litho.annotations.Prop
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange

@MountSpec
object CheckboxComponentSpec {
  @OnCreateMountContent
  fun onCreateMountContent(c: ComponentContext): CheckBox = CheckBox(c)

  @OnMount
  fun onMount(
      c: ComponentContext,
      checkbox: CheckBox,
      @Prop label: String,
      @Prop checked: Boolean,
      @Prop checkedListener: (Boolean) -> Unit) {
    checkbox.text = label
    checkbox.isChecked = checked
    checkbox.onCheckedChange { _, isChecked ->
      checkedListener(isChecked)
    }
  }
}