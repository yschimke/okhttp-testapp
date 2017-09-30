package com.squareup.okhttptestapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.squareup.okhttptestapp.R.layout
import com.squareup.okhttptestapp.github.IssuesQuery
import com.squareup.okhttptestapp.github.issues
import kotlinx.android.synthetic.main.fragment_main.execute

class IssuesFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(layout.fragment_main, container, false)

    return rootView
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    Log.i(MainActivity.TAG, "$execute")

    execute.setOnClickListener {
      runGraphQLQuery()
    }
  }

  private fun runGraphQLQuery() {
    networkClients().apolloClient.query(
        IssuesQuery.builder().owner("square").repositoryName("okhttp").build()).responseFetcher(
        ApolloResponseFetchers.NETWORK_ONLY).observable().subscribe(
        { response ->
          Log.i(MainActivity.TAG, response.issues().joinToString("\n"))
        }, { Log.i(MainActivity.TAG, "failed: ", it) })
  }
}