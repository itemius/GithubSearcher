package com.frostrabbit.githubsearcher.feature.search

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import io.anyline.camera.CameraPermissionHelper
import com.android.volley.toolbox.Volley

import android.widget.Toast
import com.android.volley.Request

import org.json.JSONObject

import com.android.volley.toolbox.StringRequest
import org.json.JSONException

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.app.ActivityOptionsCompat
import android.widget.LinearLayout
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import com.frostrabbit.githubsearcher.R
import com.frostrabbit.githubsearcher.feature.details.UserDetailsActivity
import com.frostrabbit.githubsearcher.adapter.UserListAdapter
import com.frostrabbit.githubsearcher.model.GithubUser


class SearchActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_OPEN_CAMERA = 1
    private var cameraPermissionHelper: CameraPermissionHelper? = null

    private var simpleList: ListView? = null
    private var searchView: SearchView? = null
    private var placeholder: TextView? = null
    private lateinit var loader: ProgressBar

    private var userList: ArrayList<GithubUser> = ArrayList<GithubUser>()
    private var currentPage: Int = 0

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            searchView?.setQuery(data?.getStringExtra("DATA"), false)
            searchView?.isIconified = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        simpleList = findViewById<View>(R.id.simpleListView) as ListView
        searchView = findViewById<View>(R.id.searchView) as SearchView
        loader = findViewById(R.id.progressBar)
        placeholder = findViewById<View>(R.id.placeholder) as TextView

        searchView!!.setOnClickListener(View.OnClickListener {
            searchView?.isIconified = false
        })

        val button: FloatingActionButton = findViewById(R.id.floating_action_button)
        button.setOnClickListener(View.OnClickListener {
            onButtonClick()
        })

        cameraPermissionHelper = CameraPermissionHelper(this)

        val adapter = UserListAdapter(applicationContext, userList)
        simpleList!!.adapter =  adapter

        simpleList!!.setOnItemClickListener { parent, view, position, id ->
            val user = adapter.getItemAtPosition(position) // The item that was clicked
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("login", user.login)
            intent.putExtra("avatar", user.avatarUrl)
            val options = ActivityOptionsCompat.makeScaleUpAnimation(
                view, 0, 0, view.width, view.height
            ).toBundle()

            ActivityCompat.startActivity(this, intent, options)
        }

        simpleList!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            private var currentVisibleItemCount = 0
            private var currentScrollState = 0
            private var currentFirstVisibleItem = 0
            private var totalItem = 0
            private var lBelow: LinearLayout? = null

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                currentScrollState = scrollState
                this.isScrollCompleted()
            }

            override fun onScroll(
                view: AbsListView?, firstVisibleItem: Int,
                visibleItemCount: Int, totalItemCount: Int
            ) {
                currentFirstVisibleItem = firstVisibleItem
                currentVisibleItemCount = visibleItemCount
                totalItem = totalItemCount
            }

            private fun isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                    && this.currentScrollState == SCROLL_STATE_IDLE) {
                    currentPage += 1
                    loadUserList(searchView!!.query.toString(), currentPage)
                }
            }
        })

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                userList.clear()
                currentPage = 1
                loadUserList(p0!!, currentPage)
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                placeholder!!.visibility = View.GONE
                userList.clear()
                val adapter = UserListAdapter(applicationContext, userList)
                simpleList!!.adapter =  adapter
                return false
            }
        })

    }

    fun showLoader() {
        loader.visibility = View.VISIBLE
    }

    fun hideLoader() {
        loader.visibility = View.GONE
    }

    private fun onButtonClick() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_OPEN_CAMERA);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_OPEN_CAMERA
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, ScanActivity::class.java)
            resultLauncher.launch(intent)
        } else {
            cameraPermissionHelper!!.showPermissionMessage("You should enable camera permission to scan via Anyline SDK")
        }
    }

    private fun loadUserList(query: String, page: Int, perPage: Int = 30) {
        val progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        progressBar.visibility = View.VISIBLE

        val urlString = getString(R.string.github_api_search_url) +
                "?q=" + query + "&page=" + page.toString() + "&per_page=" + perPage.toString()

        val stringRequest = StringRequest(
            Request.Method.GET, urlString,
            { response ->
                progressBar.visibility = View.INVISIBLE
                try {
                    val obj = JSONObject(response)
                    val userArray = obj.getJSONArray("items")
                    for (i in 0 until userArray.length()) {
                        val userObject = userArray.getJSONObject(i)

                        val user = GithubUser(avatarUrl = userObject.getString("avatar_url"),
                            id = userObject.getInt("id"),
                            login = userObject.getString("login"))
                        userList.add(user)
                    }
                    val adapter = UserListAdapter(applicationContext, userList)
                    simpleList!!.adapter =  adapter
                    if (page > 1) {
                        val firstVisibleItem: Int = simpleList!!.firstVisiblePosition
                        val pos = 0
                        simpleList!!.setSelectionFromTop(firstVisibleItem + adapter.count - perPage, pos)
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) { error -> Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show() }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }


}