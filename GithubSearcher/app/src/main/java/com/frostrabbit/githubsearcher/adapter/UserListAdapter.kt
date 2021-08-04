package com.frostrabbit.githubsearcher.adapter

import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.frostrabbit.githubsearcher.R
import com.frostrabbit.githubsearcher.model.GithubUser
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso


class UserListAdapter(private val context: Context, private val dataSource: ArrayList<GithubUser>): BaseAdapter() {

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView: View? = convertView
        val githubUser: GithubUser = getItem(position) as GithubUser

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_githubuser, parent, false)
        }
        val userLogin = convertView!!.findViewById(R.id.itemName) as TextView
        userLogin.text = githubUser.login

        val userImage = convertView!!.findViewById(R.id.itemImage) as ShapeableImageView
        Picasso.get().load(githubUser.avatarUrl).into(userImage)

        return convertView
    }

    fun getItemAtPosition(position: Int): GithubUser {
        return getItem(position) as GithubUser
    }
}

