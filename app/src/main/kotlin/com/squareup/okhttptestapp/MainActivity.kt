package com.squareup.okhttptestapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.fetcher.ApolloResponseFetchers.*
import com.squareup.okhttptestapp.github.IssuesQuery
import com.squareup.okhttptestapp.github.issues
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.fragment_main.execute

class MainActivity : AppCompatActivity() {
  private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

    container.adapter = mSectionsPagerAdapter
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  companion object {
    var TAG = "MainActivity"
  }

  /**
   * A [FragmentPagerAdapter] that returns a fragment corresponding to
   * one of the sections/tabs/pages.
   */
  inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      // getItem is called to instantiate the fragment for the given page.
      // Return a PlaceholderFragment (defined as a static inner class below).
      return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getCount(): Int {
      // Show 3 total pages.
      return 3
    }
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  class PlaceholderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
      val rootView = inflater.inflate(R.layout.fragment_main, container, false)

      return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
      Log.i(TAG, "$execute")

      execute.setOnClickListener {
        runGraphQLQuery()
      }
    }

    private fun runGraphQLQuery() {
      networkClients().apolloClient.query(
          IssuesQuery.builder().owner("square").repositoryName("okhttp").build()).responseFetcher(
          NETWORK_ONLY).observable().subscribe(
          { response -> Log.i(TAG, response.issues().joinToString("\n")) }, { Log.i(TAG, "failed: ", it) })
    }

    companion object {
      /**
       * The fragment argument representing the section number for this
       * fragment.
       */
      private val ARG_SECTION_NUMBER = "section_number"

      /**
       * Returns a new instance of this fragment for the given section
       * number.
       */
      fun newInstance(sectionNumber: Int): PlaceholderFragment {
        val fragment = PlaceholderFragment()
        val args = Bundle()
        args.putInt(ARG_SECTION_NUMBER, sectionNumber)
        fragment.arguments = args
        return fragment
      }
    }
  }
}
