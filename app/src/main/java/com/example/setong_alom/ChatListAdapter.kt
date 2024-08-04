import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.setong_alom.ChatList
import com.example.setong_alom.databinding.ChatListBinding

class ChatListAdapter(private val originalList: ArrayList<ChatList>) : RecyclerView.Adapter<ChatListAdapter.ChattingListViewHolder>() {

    private var filteredList: ArrayList<ChatList> = ArrayList(originalList)

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingListViewHolder {
        val binding = ChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChattingListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: ChattingListViewHolder, position: Int) {
        val currentItem = filteredList[position]
        holder.bind(currentItem)
    }

    inner class ChattingListViewHolder(private val binding: ChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        val profileImageView = binding.profile
        val username = binding.userName
        val content = binding.content
        val time = binding.time

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }

        fun bind(item: ChatList) {
            profileImageView.setImageResource(item.profile)
            username.text = item.name
            content.text = item.content
            time.text = item.time
        }
    }

    // 필터 메소드
    fun filter(query: String) {
        val lowerCaseQuery = query.toLowerCase()
        filteredList = if (query.isEmpty()) {
            ArrayList(originalList)
        } else {
            val resultList = ArrayList<ChatList>()
            for (item in originalList) {
                if (item.name.toLowerCase().contains(lowerCaseQuery)) {
                    resultList.add(item)
                }
            }
            resultList
        }
        notifyDataSetChanged()
    }
}
