package com.ht.robotvacuumapp.model.repository;

import com.ht.robotvacuumapp.model.entity.RobotDevice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DemoRobotRepository {
    private final List<RobotDevice> devices = new ArrayList<>();

    public DemoRobotRepository() { restoreSamples(); }

    public List<RobotDevice> getDevices() {
        return Collections.unmodifiableList(new ArrayList<>(devices));
    }

    public void add(String id) {
        devices.add(new RobotDevice(id, id, "Phòng mới", "Sẵn sàng", 100, false));
    }

    public void clear() { devices.clear(); }

    public void restoreSamples() {
        devices.clear();
        devices.add(new RobotDevice("1", "Robot_01", "Phòng khách", "Sẵn sàng", 83, false));
        devices.add(new RobotDevice("2", "Robot_02", "Phòng bếp", "Đang sạc", 100, false));
        devices.add(new RobotDevice("3", "Robot_03", "Phòng ngủ 2", "Chờ", 45, true));
    }
}
