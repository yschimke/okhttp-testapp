package com.squareup.okhttptestapp.spec

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.baulsupp.oksocial.network.IPvMode
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.MountSpec
import com.facebook.litho.annotations.OnCreateMountContent
import com.facebook.litho.annotations.OnMount
import com.facebook.litho.annotations.Prop

@MountSpec
object IpModeComponentSpec {
  @OnCreateMountContent
  fun onCreateMountContent(c: ComponentContext): Spinner = Spinner(c)

  @OnMount
  fun onMount(
      c: ComponentContext,
      spinner: Spinner,
      @Prop initialMode: IPvMode,
      @Prop selectionListener: (IPvMode) -> Unit) {
    val specs = IPvMode.values()
    val arrayAdapter: SpinnerAdapter = ArrayAdapter<IPvMode>(c,
        android.R.layout.simple_spinner_dropdown_item, specs)
    spinner.adapter = arrayAdapter
    spinner.setSelection(specs.indexOf(initialMode))
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onNothingSelected(parent: AdapterView<*>?) {
      }

      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mode = specs[position]

        // TODO is this correct? or needs state?
        if (mode != initialMode) {
          selectionListener(mode)
        }
      }
    }
  }
}