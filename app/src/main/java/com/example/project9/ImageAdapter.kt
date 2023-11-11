package com.example.project9

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(private val images: List<Uri>, private val onClick: (Uri) -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>()
{

    /**
     * Provides a reference to the views for each data item. Complex data items may need more than one view per item
     * and you provide access to all the views for a data item in a view holder
     */
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {val imageView: ImageView = view.findViewById(R.id.image_view_item)}

    /**
     * Creates new views. Inflates the item layout and returns a ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    /**
     * Replaces the contents of a view. Binds data to the ViewHolder
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int)
    {
        Glide.with(holder.imageView.context)
            .load(images[position])
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {onClick(images[position])}
    }

    /**
     * Returns the size of the dataset
     *
     * @return The total number of items in this adapter
     */
    override fun getItemCount() = images.size
}