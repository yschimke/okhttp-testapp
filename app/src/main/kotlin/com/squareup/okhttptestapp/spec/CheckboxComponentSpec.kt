package com.squareup.okhttptestapp.spec

import android.util.Log
import android.widget.CheckBox
import com.facebook.litho.ComponentContext
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.MountSpec
import com.facebook.litho.annotations.OnCreateInitialState
import com.facebook.litho.annotations.OnCreateMountContent
import com.facebook.litho.annotations.OnMount
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.State
import com.squareup.okhttptestapp.RequestOptions
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange

@MountSpec
object CheckboxComponentSpec {
//  @OnCreateInitialState
//  fun createInitialState(c: ComponentContext,
//      checked: StateValue<Boolean>,
//      @Prop initialChecked: Boolean) {
//    checked.set(initialChecked)
//  }

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
      Log.i("CheckboxComponent", "$isChecked")
      checkedListener(isChecked)
    }
  }
}