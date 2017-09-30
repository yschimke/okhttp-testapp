package com.squareup.okhttptestapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.okhttptestapp.R.layout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.execute
import kotlinx.android.synthetic.main.fragment_main.log

class TestQueryFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(layout.fragment_main, container, false)

    return rootView
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    Log.i(MainActivity.TAG, "$execute")

    execute.setOnClickListener {
      val call = networkClients().testClient.newCall(TestSetup.createRequest())

      Observable.fromCallable({
        val result = call.execute()
        Log.i(TAG, result.headers().toString())
        result.body()!!.string()
      })
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(this::showResult, this::showError)
    }
  }

  private fun showError(t: Throwable) {
    Log.i(TAG, "failed", t)
    log.text = t.toString()
  }

  fun showResult(result: String) {
    Log.i(TAG, result)
    log.text = result
  }

  companion object {
    val TAG = "TestQueryFragment"
  }
}