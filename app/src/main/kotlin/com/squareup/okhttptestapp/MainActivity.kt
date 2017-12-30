package com.squareup.okhttptestapp

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {
  private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

    container.adapter = mSectionsPagerAdapter

    installGmsProvider();
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

  private fun installGmsProvider() {
    val gApi = GoogleApiAvailability.getInstance()
    var resultCode = gApi.isGooglePlayServicesAvailable(this)

    if (resultCode != ConnectionResult.SUCCESS) {
      if (gApi.isUserResolvableError(resultCode)) {
        gApi.getErrorDialog(this, resultCode, 9000).show()
      }
      resultCode = gApi.isGooglePlayServicesAvailable(this)
    }

    if (resultCode == ConnectionResult.SUCCESS) {
      ProviderInstaller.installIfNeededAsync(applicationContext,
          object : ProviderInstallListener {
            override fun onProviderInstallFailed(p0: Int, p1: Intent?) {
              Log.w("OkHttpTestApp", "provider install failed")
            }

            override fun onProviderInstalled() {
              Log.i("OkHttpTestApp", "provider installed")
            }
          })
    }
  }

  companion object {
    var TAG = "MainActivity"
  }

  inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      return TestQueryFragment()
    }

    override fun getCount(): Int {
      return 1
    }
  }

}
