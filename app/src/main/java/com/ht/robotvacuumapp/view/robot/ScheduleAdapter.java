package com.ht.robotvacuumapp.view.robot;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ht.robotvacuumapp.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    public interface OnScheduleActionListener {
        void onToggle(ScheduleItem item, boolean isChecked, int position);
        void onDelete(ScheduleItem item, int position);
    }

    private final List<ScheduleItem> items;
    private final OnScheduleActionListener actionListener;

    public ScheduleAdapter(List<ScheduleItem> items, OnScheduleActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_card, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.tvTimezone.setText(item.getTimezone());
        holder.tvTime.setText(item.getTime());
        holder.tvMode.setText(item.getMode());
        holder.tvRepeat.setText(item.getRepeatDays());

        // Reset translation on bind
        holder.layoutCardForeground.setTranslationX(0f);

        holder.switchSchedule.setOnCheckedChangeListener(null);
        holder.switchSchedule.setChecked(item.isEnabled());
        holder.switchSchedule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEnabled(isChecked);
            if (actionListener != null) {
                actionListener.onToggle(item, isChecked, holder.getBindingAdapterPosition());
            }
        });

        // Delete button actions
        View.OnClickListener deleteClickListener = v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                holder.layoutCardForeground.animate().translationX(0f).setDuration(150).start();
                actionListener.onDelete(item, pos);
            }
        };
        holder.btnDeleteLeft.setOnClickListener(deleteClickListener);
        holder.btnDeleteRight.setOnClickListener(deleteClickListener);

        // Setup swipe-to-reveal gesture
        setupSwipeGesture(holder);
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

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        final View layoutCardForeground;
        final View btnDeleteLeft;
        final View btnDeleteRight;
        final TextView tvTimezone;
        final TextView tvTime;
        final TextView tvMode;
        final TextView tvRepeat;
        final SwitchCompat switchSchedule;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCardForeground = itemView.findViewById(R.id.layout_card_foreground);
            btnDeleteLeft = itemView.findViewById(R.id.btn_delete_schedule_left);
            btnDeleteRight = itemView.findViewById(R.id.btn_delete_schedule_right);
            tvTimezone = itemView.findViewById(R.id.tv_schedule_timezone);
            tvTime = itemView.findViewById(R.id.tv_schedule_time);
            tvMode = itemView.findViewById(R.id.tv_schedule_mode);
            tvRepeat = itemView.findViewById(R.id.tv_schedule_repeat);
            switchSchedule = itemView.findViewById(R.id.switch_schedule);
        }
    }
}
