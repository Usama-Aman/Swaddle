package com.android.swaddle.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemFileBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.maskeditor.getFileExtension
import com.android.swaddle.utils.maskeditor.getFileName
import com.bumptech.glide.Glide


class CustodyDocumentAdapter(
    private val mContext: Context,
    var documentPath: ArrayList<String>,
    var showOnly: Boolean = false
) :
    RecyclerView.Adapter<CustodyDocumentAdapter.FilesViewHolder>() {
    private var clickListener: CLickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemFileBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_file,
            parent,
            false
        )
        return FilesViewHolder(
            binding,
            this@CustodyDocumentAdapter,
            mContext
        )
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        holder.onBind(Uri.parse(documentPath[position]), mContext, showOnly, documentPath[position])
        holder.clickListener()
    }

    override fun getItemCount(): Int {
        return documentPath.size
    }

    fun setData(list: ArrayList<String>) {
        documentPath = list
        notifyDataSetChanged()
    }

    fun concatinateData(list: ArrayList<String>) {
        documentPath = list
        notifyDataSetChanged()
    }


    class FilesViewHolder(
        private val binding: ItemFileBinding,
        private val adapter: CustodyDocumentAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(uri: Uri, mContext: Context, showOnly: Boolean, documentPath: String) {
            if (position == adapter.documentPath.size - 1) {
                binding.view.viewGone()
            } else {
                binding.view.viewVisible()
            }
            binding.tvFileName.text = getFileName(uri, context)

            if (showOnly) {
                binding.imgDownload.viewVisible()
                binding.ivCross.viewGone()
            } else {
                binding.imgDownload.viewGone()
                binding.ivCross.viewVisible()
            }

            if (documentPath.contains("custody_document"))
                Glide.with(mContext).load(Constants.IMG_BASE_PATH + uri.path).centerCrop()
                    .into(binding.img)
            else
                Glide.with(context).load(documentPath).into(binding.img)

            try {

                if (getFileExtension(uri, context) == "txt") {
                    Glide.with(mContext).load(R.drawable.ic_txt).into(binding.imgPdf)
                } else if (getFileExtension(uri, context) == "xls") {
                    Glide.with(mContext).load(R.drawable.ic_xls).into(binding.imgPdf)
                } else if (getFileExtension(uri, context) == "png") {
                    Glide.with(mContext).load(R.drawable.ic_png).into(binding.imgPdf)
                } else if (getFileExtension(uri, context) == "pdf") {
                    Glide.with(mContext).load(R.drawable.ic_pdf).into(binding.imgPdf)
                } else if (getFileExtension(uri, context) == "jpg"
                    || getFileExtension(uri, context) == "jpeg"
                ) {
                    Glide.with(mContext).load(R.drawable.ic_jpg).into(binding.imgPdf)
                } else if (getFileExtension(uri, context) == "docx"
                    || getFileExtension(uri, context) == "doc"
                ) {
                    Glide.with(mContext).load(R.drawable.ic_doc).into(binding.imgPdf)
                } else {
                    Glide.with(mContext).load(R.drawable.ic_pdf).into(binding.imgPdf)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun clickListener() {
            binding.rootLayout.setOnClickListener {
                adapter.clickListener?.onItemClick(adapterPosition)
            }
            binding.ivCross.setOnClickListener {
                adapter.clickListener?.onRemoveClick(adapterPosition)
            }
            binding.imgDownload.setOnClickListener {
                adapter.clickListener?.onDownloadClick(adapterPosition)
            }
        }
    }


    fun setListener(listener: CLickListener) {
        clickListener = listener
    }


    interface CLickListener {
        fun onItemClick(pos: Int)
        fun onRemoveClick(pos: Int)
        fun onDownloadClick(position: Int)

    }
}