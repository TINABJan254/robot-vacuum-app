package com.ht.robotvacuumapp.view.robot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ht.robotvacuumapp.model.entity.RobotCommand;

public class JoystickLayout extends ConstraintLayout {

    public interface Listener {
        void onDirectionChanged(@Nullable RobotCommand.Direction direction);
        void onKnobTap();
    }

    private static final double HYSTERESIS_DEG = 12;

    private final float deadZone;
    private final int touchSlop;

    @Nullable private View knob;
    @Nullable private Listener listener;
    @Nullable private RobotCommand.Direction current;
    private float downX, downY;
    private boolean dragged;
    private boolean tracking;

    public JoystickLayout(@NonNull Context context) { this(context, null); }

    public JoystickLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = context.getResources().getDisplayMetrics().density;
        deadZone = 26 * density;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setKnob(@Nullable View knob) { this.knob = knob; }

    public void setListener(@Nullable Listener listener) { this.listener = listener; }

    @Nullable public RobotCommand.Direction getCurrentDirection() { return current; }

    /** Drops any active drag (e.g. screen hidden) and recenters the knob. Does not notify. */
    public void reset() {
        tracking = false;
        current = null;
        recenterKnob(false);
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isEnabled() || tracking;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!isEnabled()) return false;
                tracking = true;
                dragged = false;
                downX = event.getX();
                downY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);

                if (!isOnKnob(downX, downY)) update(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!tracking) return false;
                if (!dragged && Math.hypot(event.getX() - downX, event.getY() - downY) > touchSlop) dragged = true;
                if (dragged || !isOnKnob(downX, downY)) update(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                if (!tracking) return false;
                boolean tap = !dragged && isOnKnob(downX, downY);
                finish();
                if (tap && listener != null) listener.onKnobTap();
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (!tracking) return false;
                finish();
                return true;
            default:
                return tracking;
        }
    }

    private void finish() {
        tracking = false;
        recenterKnob(true);
        setDirection(null);
    }

    private void update(float x, float y) {
        float dx = x - getWidth() / 2f;
        float dy = y - getHeight() / 2f;
        moveKnob(dx, dy);
        double distance = Math.hypot(dx, dy);
        setDirection(distance < deadZone ? null : directionFor(dx, dy));
    }

    private RobotCommand.Direction directionFor(float dx, float dy) {
        // 0° = right, 90° = down (screen coordinates).
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (current != null && angleDistance(angle, centerAngle(current)) <= 45 + HYSTERESIS_DEG) return current;
        if (angle >= -45 && angle < 45) return RobotCommand.Direction.RIGHT;
        if (angle >= 45 && angle < 135) return RobotCommand.Direction.BACKWARD;
        if (angle >= -135 && angle < -45) return RobotCommand.Direction.FORWARD;
        return RobotCommand.Direction.LEFT;
    }

    private static double centerAngle(RobotCommand.Direction d) {
        switch (d) {
            case RIGHT: return 0;
            case BACKWARD: return 90;
            case FORWARD: return -90;
            default: return 180;
        }
    }

    private static double angleDistance(double a, double b) {
        double diff = Math.abs(a - b) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    private void setDirection(@Nullable RobotCommand.Direction direction) {
        if (direction == current) return;
        current = direction;
        performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
        if (listener != null) listener.onDirectionChanged(direction);
    }

    private boolean isOnKnob(float x, float y) {
        if (knob == null) return false;
        float r = Math.max(knob.getWidth(), knob.getHeight()) / 2f;
        return Math.hypot(x - getWidth() / 2f, y - getHeight() / 2f) <= r;
    }

    private void moveKnob(float dx, float dy) {
        if (knob == null) return;
        float max = Math.min(getWidth(), getHeight()) / 2f - Math.max(knob.getWidth(), knob.getHeight()) / 2f;
        double distance = Math.hypot(dx, dy);
        if (distance > max && distance > 0) {
            dx = (float) (dx * max / distance);
            dy = (float) (dy * max / distance);
        }
        knob.animate().cancel();
        knob.setTranslationX(dx);
        knob.setTranslationY(dy);
        knob.setPressed(true);
    }

    private void recenterKnob(boolean animate) {
        if (knob == null) return;
        knob.setPressed(false);
        knob.animate().cancel();
        if (animate) {
            knob.animate().translationX(0).translationY(0).setDuration(150).start();
        } else {
            knob.setTranslationX(0);
            knob.setTranslationY(0);
        }
    }
}
