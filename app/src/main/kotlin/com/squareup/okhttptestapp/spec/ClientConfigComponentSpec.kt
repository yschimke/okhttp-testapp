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
import com.makeramen.litho.component
import com.makeramen.litho.layout
import com.makeramen.litho.row
import com.squareup.okhttptestapp.model.ClientOptions

@LayoutSpec
object ClientConfigComponentSpec {
  @OnCreateInitialState
  fun createInitialState(c: ComponentContext,
      clientOptions: StateValue<ClientOptions>,
      @Prop initialClientOptions: ClientOptions) {
    clientOptions.set(initialClientOptions)
  }

  @OnCreateLayout
  fun onCreateLayout(
      c: ComponentContext,
      @State clientOptions: ClientOptions,
      @Prop gmsAvailable: Boolean,
      @Prop configListener: (ClientOptions) -> Unit): ComponentLayout =
      layout {
        column(c) {
          children {
            row(c) {
              children {
                component(c, CheckboxComponent::create) {
                  label("GMS")
                  checked(gmsAvailable && clientOptions.gms)
                  widthDip(80f)
                  heightDip(40f)
                  available(gmsAvailable)
                  checkedListener {
                    val newClientOptions = clientOptions.copy(gms = it)
                    ClientConfigComponent.updateClientOptions(c, newClientOptions)
                    configListener(newClientOptions)
                  }
                }
                component(c, ConnectionSpecComponent::create) {
                  initialConfigSpec(clientOptions.configSpec)
                  widthDip(160f)
                  heightDip(40f)
                  selectionListener {
                    val newClientOptions = clientOptions.copy(configSpec = it)
                    ClientConfigComponent.updateClientOptions(c, newClientOptions)
                    configListener(newClientOptions)
                  }
                }
                component(c, CheckboxComponent::create) {
                  label("Z")
                  checked(clientOptions.zipkin)
                  widthDip(100f)
                  heightDip(40f)
                  checkedListener {
                    val newClientOptions = clientOptions.copy(zipkin = it)
                    ClientConfigComponent.updateClientOptions(c, newClientOptions)
                    configListener(newClientOptions)
                  }
                }
                component(c, CheckboxComponent::create) {
                  label("AOP")
                  checked(clientOptions.optimized)
                  widthDip(100f)
                  heightDip(40f)
                  checkedListener {
                    val newClientOptions = clientOptions.copy(optimized = it)
                    ClientConfigComponent.updateClientOptions(c, newClientOptions)
                    configListener(newClientOptions)
                  }
                }
              }
            }
          }
        }
      }

  @OnUpdateState
  fun updateClientOptions(
      clientOptions: StateValue<ClientOptions>,
      @Param newClientOptions: ClientOptions) {
    clientOptions.set(newClientOptions)
  }
}