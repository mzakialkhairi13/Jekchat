package com.mzakialkhairi.jekchat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mzakialkhairi.jekchat.databinding.ActivityMainBinding
import com.mzakialkhairi.jekchat.fragments.ChatsFragment
import com.mzakialkhairi.jekchat.fragments.SearchFragment
import com.mzakialkhairi.jekchat.fragments.SettingFragment
import com.mzakialkhairi.jekchat.model.Chat
import com.mzakialkhairi.jekchat.model.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    var refUsers : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        setSupportActionBar(findViewById(R.id.toolbar_main))
        supportActionBar?.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tablayout)
        val viewPager : ViewPager = findViewById(R.id.view_pager)

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                var viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

                var countUnreadMessage = 0
                for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.getIsseen()!!)
                    {
                        countUnreadMessage += 1
                    }
                }

                if (countUnreadMessage == 0){
                      viewPagerAdapter.addFragment(ChatsFragment(),"Chats")
                }
                else{
                    viewPagerAdapter.addFragment(ChatsFragment(),"($countUnreadMessage) Chats")
                }
                    viewPagerAdapter.addFragment(SearchFragment(),"Search")
                    viewPagerAdapter.addFragment(SettingFragment(),"Account")

                    viewPager.adapter = viewPagerAdapter
                    tabLayout.setupWithViewPager(viewPager)
            }

        })


        //display username and profile picture
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)
                    if (user != null) {
                        Picasso.get().load(user.getProfile()).into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

                TODO("Not yet implemented")
            }

        })


    }


    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){

        private val fragments : ArrayList <Fragment>
        private val titles : ArrayList <String>

        init{
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment,title : String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }
}