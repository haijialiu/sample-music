package com.liuhaijia229350323.samplemusic

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.liuhaijia229350323.samplemusic.data.Music

class MusicListFragment : Fragment() {

    companion object {
        fun newInstance() = MusicListFragment()
    }

    private lateinit var viewModel: MusicListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]
        // TODO: Use the ViewModel
    }
    private class MusicHolder(itemTextView: TextView)
        :RecyclerView.ViewHolder(itemTextView){
            val bindTitle:(CharSequence) -> Unit = itemTextView::setText
        }
    private class MusicListAdapter(private val musics:List<Music>)
        :RecyclerView.Adapter<MusicHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
            val textView = TextView(parent.context)
            return MusicHolder(textView)
        }

        override fun getItemCount() = musics.size

        override fun onBindViewHolder(holder: MusicHolder, position: Int) {
            val music = musics[position]
            holder.bindTitle(music.musicName)
        }

    }

}