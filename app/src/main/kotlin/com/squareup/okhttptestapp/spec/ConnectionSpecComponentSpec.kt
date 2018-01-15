package com.squareup.okhttptestapp.spec

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.MountSpec
import com.facebook.litho.annotations.OnCreateMountContent
import com.facebook.litho.annotations.OnMount
import com.facebook.litho.annotations.Prop
import com.squareup.okhttptestapp.model.allSpecs
import com.squareup.okhttptestapp.model.ConfigSpec

@MountSpec
object ConnectionSpecComponentSpec {
  @OnCreateMountContent
  fun onCreateMountContent(c: ComponentContext): Spinner = Spinner(c)

  @OnMount
  fun onMount(
      c: ComponentContext,
      spinner: Spinner,
      @Prop initialConfigSpec: ConfigSpec,
      @Prop selectionListener: (ConfigSpec) -> Unit) {
    val specs = allSpecs
    val arrayAdapter: SpinnerAdapter = ArrayAdapter<ConfigSpec>(c,
        android.R.layout.simple_spinner_dropdown_item, specs)
    spinner.adapter = arrayAdapter
    spinner.setSelection(specs.indexOf(initialConfigSpec))
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>?) {
      }

      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val configSpec = specs[position]

        if (configSpec != initialConfigSpec) {
          selectionListener(configSpec)
        }
      }
    }
  }
}