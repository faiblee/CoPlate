package com.faible.coplate.family;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.api.FamilyApi;
import com.faible.coplate.api.RetrofitClient;
import com.faible.coplate.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<User> memberList = new ArrayList<>();
    private OnMemberClickListener listener;
    private OnKickListener kickListener;
    private boolean canKick = false;
    private String currentUserId;
    private String currentFamilyId;

    public interface OnMemberClickListener {
        void onMemberClick(User user);
    }

    public interface OnKickListener {
        void onKick(String userId);
    }

    public MemberAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setCanKick(boolean canKick) {
        this.canKick = canKick;
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void setCurrentFamilyId(String familyId) {
        this.currentFamilyId = familyId;
    }

    public void setOnKickListener(OnKickListener listener) {
        this.kickListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = memberList.get(position);
        String displayName = user.getName() != null ? user.getName() : user.getUsername();
        holder.memberNameText.setText(displayName);

        // Логика отображения кнопки удаления
        if (canKick) {
            // Если текущий пользователь - владелец
            if (user.getId().equals(currentUserId)) {
                // Кнопка скрыта для владельца (он не может себя исключить, только распустить семью)
                holder.kickButton.setVisibility(View.GONE);
            } else {
                // Владелец может исключать других участников
                holder.kickButton.setVisibility(View.VISIBLE);
                holder.kickButton.setText("Исключить");
                holder.kickButton.setOnClickListener(v -> {
                    if (kickListener != null) {
                        kickListener.onKick(user.getId());
                    }
                });
            }
        } else {
            // Если текущий пользователь не владелец
            if (user.getId().equals(currentUserId)) {
                // Кнопка "Покинуть семью" (вызывает kick со своим ID)
                holder.kickButton.setVisibility(View.VISIBLE);
                holder.kickButton.setText("Покинуть семью");
                holder.kickButton.setOnClickListener(v -> {
                    if (kickListener != null) {
                        kickListener.onKick(currentUserId);
                    }
                });
            } else {
                holder.kickButton.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemberClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void setMemberList(List<User> members) {
        memberList = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameText;
        Button kickButton;

        MemberViewHolder(View itemView) {
            super(itemView);
            memberNameText = itemView.findViewById(R.id.memberName);
            kickButton = itemView.findViewById(R.id.kickButton);
        }
    }
}
