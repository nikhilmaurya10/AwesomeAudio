package com.testaudio.awesomeaudio.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.testaudio.awesomeaudio.R
import com.testaudio.awesomeaudio.data.model.AudioModel
import kotlinx.android.synthetic.main.layout_audio.view.*

class AudioListAdapter(
    private val context: Context,
    private var audioList: List<AudioModel>,
    private val articleClickListner: ((String) -> Unit)
) : RecyclerView.Adapter<AudioListAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_audio, parent, false)
        return MyViewHolder(v, articleClickListner)
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(audioList[position])
    }

    fun setNewList(it: List<AudioModel>) {
        audioList = it
        notifyDataSetChanged()
    }


    class MyViewHolder(itemView: View, private val articleClickListner: ((String) -> Unit)) : RecyclerView.ViewHolder(itemView) {
        fun bind (audio : AudioModel) = with(itemView){
            textView.text = audio.fileName
            textView3.text = audio.synced.toString()
            itemView.setOnClickListener {
                articleClickListner(audio.fileLocalPath)
            }

        }
    }
}