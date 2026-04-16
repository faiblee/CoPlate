package com.faible.coplate.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.model.User;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<User> memberList;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(User user);
        // Можно добавить onDeleteClick, если нужно удалять участников из списка
    }

    public MemberAdapter(OnMemberClickListener listener) {
        this.memberList = new ArrayList<>();
        this.listener = listener;
    }

    public void setMemberList(List<User> list) {
        this.memberList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Убедитесь, что имя файла layout совпадает с вашим XML (например, item_member.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = memberList.get(position);

        // Отображаем имя (если name пустое, можно показать username)
        String displayName = (user.getName() != null && !user.getName().isEmpty())
                ? user.getName()
                : user.getUsername();
        holder.nameText.setText(displayName);

        // Обработка клика по элементу
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMemberClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageView memberIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.memberName);
            memberIcon = itemView.findViewById(R.id.memberIcon);
        }
    }
}
