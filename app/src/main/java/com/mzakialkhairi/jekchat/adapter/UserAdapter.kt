package com.mzakialkhairi.jekchat.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mzakialkhairi.jekchat.MessageChatActivity
import com.mzakialkhairi.jekchat.R
import com.mzakialkhairi.jekchat.model.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.view.*


class UserAdapter(
    mContex : Context,
    mUsers : List<Users>,
    isChatCheck : Boolean
    ) : RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{

    private val mContex : Context
    private val mUsers : List<Users>
    private var isChatCheck : Boolean

    init{
        this.isChatCheck = isChatCheck
        this.mContex = mContex
        this.mUsers = mUsers
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var usernameTxt : TextView
        var ProfileImageTxt : CircleImageView
        var OnlineTxt : CircleImageView
        var OfflineTxt : CircleImageView
        var lastMessageTxt : TextView
        var verified : ImageView

        init {
            usernameTxt = itemView.findViewById(R.id.item_username)
            ProfileImageTxt = itemView.findViewById(R.id.item_profile_image)
            OnlineTxt = itemView.findViewById(R.id.item_image_online)
            OfflineTxt = itemView.findViewById(R.id.item_image_offline)
            lastMessageTxt = itemView.findViewById(R.id.item_message_last)
            verified = itemView.findViewById(R.id.verified)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContex).inflate(R.layout.user_search_item_layout,viewGroup,false)
            return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users? = mUsers.get(position)
        if (user != null) {

            if (user.getLevel() == "1"){
                holder.verified.visibility = View.VISIBLE
            }

            holder.usernameTxt.text = user.getUsername()
            Picasso.get().load(user.getProfile()).into(holder.ProfileImageTxt)
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )

            val builder : AlertDialog.Builder = AlertDialog.Builder(mContex)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener{dialog, which ->  

                val intent = Intent(mContex,MessageChatActivity::class.java)
                intent.putExtra(MessageChatActivity.userIDVisit,user!!.getUID())
                mContex.startActivity(intent)

            })
            builder.show()
        }
    }
}
