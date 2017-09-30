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
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.squareup.okhttptestapp.github.IssuesQuery
import com.squareup.okhttptestapp.github.IssuesQuery.Data
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

  /**
   * The [android.support.v4.view.PagerAdapter] that will provide
   * fragments for each of the sections. We use a
   * {@link FragmentPagerAdapter} derivative, which will keep every
   * loaded fragment in memory. If this becomes too memory intensive, it
   * may be best to switch to a
   * [android.support.v4.app.FragmentStatePagerAdapter].
   */
  private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setSupportActionBar(toolbar)
    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

    // Set up the ViewPager with the sections adapter.
    container.adapter = mSectionsPagerAdapter

    val query = networkClients().apolloClient.query(
        IssuesQuery.builder().owner("square").repositoryName("okhttp").build()).responseFetcher(
        ApolloResponseFetchers.NETWORK_ONLY)

    val b: Observable<Response<Data>> = query.observable()

    b.subscribe({ response ->
      run {
        val map = response.data()?.repository()?.issues()?.nodes()?.map { it.title() }?.joinToString(
            "\n").orEmpty()
        Log.i(TAG, map)
      }
    },
        { Log.i(TAG, "failed: ", it) })
  }

  private fun networkClients() = (application as OkHttpTestApp).networkClients!!

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
