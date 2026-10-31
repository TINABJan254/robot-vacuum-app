package com.ht.robotvacuumapp.view.robot;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.ht.robotvacuumapp.R;
import com.ht.robotvacuumapp.api.dto.ScheduleResponse;
import java.util.*;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    public interface OnScheduleActionListener {
        void onToggle(long id, boolean enabled);
        void onDelete(long id);
    }
    private List<ScheduleItem> items = Collections.emptyList();
    private final OnScheduleActionListener listener;
    public ScheduleAdapter(OnScheduleActionListener listener) { this.listener = listener; setHasStableIds(true); }
    public void submitSchedules(List<ScheduleResponse> schedules, Map<Long, Boolean> pending, Set<Long> deleting) {
        List<ScheduleItem> next = new ArrayList<>();
        for (ScheduleResponse s : schedules) next.add(new ScheduleItem(s, pending.get(s.getId()), deleting.contains(s.getId())));
        List<ScheduleItem> previous = items;
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return previous.size(); }
            @Override public int getNewListSize() { return next.size(); }
            @Override public boolean areItemsTheSame(int oldIndex, int newIndex) { return previous.get(oldIndex).getId() == next.get(newIndex).getId(); }
            @Override public boolean areContentsTheSame(int oldIndex, int newIndex) {
                ScheduleItem a = previous.get(oldIndex), b = next.get(newIndex);
                return a.getTime().equals(b.getTime()) && a.getDays().equals(b.getDays())
                        && a.isEnabled() == b.isEnabled() && a.isBusy() == b.isBusy();
            }
        });
        items = next;
        diff.dispatchUpdatesTo(this);
    }
    @Override public long getItemId(int position) { return items.get(position).getId(); }
    @NonNull @Override public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_card, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.time.setText(item.getTime());
        holder.repeat.setText(repeatLabel(holder.itemView.getContext(), item.getDays()));
        holder.layoutCardForeground.animate().cancel();
        holder.layoutCardForeground.setTranslationX(0f);
        holder.layoutCardForeground.setEnabled(!item.isBusy());
        holder.layoutCardForeground.setAlpha(item.isBusy() ? 0.6f : 1f);
        holder.btnDeleteLeft.setEnabled(!item.isBusy()); holder.btnDeleteRight.setEnabled(!item.isBusy());
        holder.toggle.setOnCheckedChangeListener(null);
        holder.toggle.setChecked(item.isEnabled());
        holder.toggle.setEnabled(!item.isBusy());
        holder.toggle.setOnCheckedChangeListener((button, checked) -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION && !items.get(current).isBusy()) listener.onToggle(items.get(current).getId(), checked);
        });
        View.OnClickListener delete = v -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION && !items.get(current).isBusy()) {
                holder.layoutCardForeground.animate().translationX(0f).setDuration(150).start();
                listener.onDelete(items.get(current).getId());
            }
        };
        holder.btnDeleteLeft.setOnClickListener(delete); holder.btnDeleteRight.setOnClickListener(delete);
        setupSwipeGesture(holder);
    }
    private String repeatLabel(Context context, List<Integer> days) {
        if (days.isEmpty()) return context.getString(R.string.schedule_no_repeat_days);
        if (days.size() == 7) return context.getString(R.string.schedule_every_day);
        if (days.equals(Arrays.asList(1, 2, 3, 4, 5))) return context.getString(R.string.schedule_days_workdays);
        if (days.equals(Arrays.asList(6, 7))) return context.getString(R.string.schedule_weekend);
        String[] labels = context.getResources().getStringArray(R.array.schedule_day_names);
        List<String> chosen = new ArrayList<>();
        for (int day : days) chosen.add(labels[day - 1]);
        return android.text.TextUtils.join(", ", chosen);
    }
    private void setupSwipeGesture(ScheduleViewHolder holder) {
        View foreground = holder.layoutCardForeground;
        float density = foreground.getContext().getResources().getDisplayMetrics().density;
        float maxSwipePx = 86f * density;
        int touchSlop = ViewConfiguration.get(foreground.getContext()).getScaledTouchSlop();

        foreground.setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0f;
            private float startY = 0f;
            private float initialTransX = 0f;
            private boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!v.isEnabled()) return false;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        initialTransX = v.getTranslationX();
                        isSwiping = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX;
                        float dy = event.getRawY() - startY;

                        if (!isSwiping && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy) * 1.2f) {
                            isSwiping = true;
                            if (v.getParent() != null) {
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }

                        if (isSwiping) {
                            float targetX = initialTransX + dx;
                            if (targetX < -maxSwipePx) targetX = -maxSwipePx;
                            if (targetX > maxSwipePx) targetX = maxSwipePx;
                            v.setTranslationX(targetX);
                            return true;
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (v.getParent() != null) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                        }

                        if (isSwiping) {
                            float currentX = v.getTranslationX();
                            if (currentX < -maxSwipePx / 3f) {
                                v.animate().translationX(-maxSwipePx).setDuration(200).start();
                            } else if (currentX > maxSwipePx / 3f) {
                                v.animate().translationX(maxSwipePx).setDuration(200).start();
                            } else {
                                v.animate().translationX(0f).setDuration(200).start();
                            }
                            isSwiping = false;
                            return true;
                        } else if (Math.abs(v.getTranslationX()) > 5f) {
                            // If tapped while open, close it
                            v.animate().translationX(0f).setDuration(200).start();
                            return true;
                        }
                        return false;
                }
                return false;
            }
        });
    }


    @Override public int getItemCount() { return items.size(); }
    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        final View layoutCardForeground, btnDeleteLeft, btnDeleteRight;
        final TextView time, repeat;
        final SwitchCompat toggle;
        ScheduleViewHolder(@NonNull View view) {
            super(view);
            layoutCardForeground = view.findViewById(R.id.layout_card_foreground);
            btnDeleteLeft = view.findViewById(R.id.btn_delete_schedule_left); btnDeleteRight = view.findViewById(R.id.btn_delete_schedule_right);
            time = view.findViewById(R.id.tv_schedule_time); repeat = view.findViewById(R.id.tv_schedule_repeat);
            toggle = view.findViewById(R.id.switch_schedule);
        }
    }
}
