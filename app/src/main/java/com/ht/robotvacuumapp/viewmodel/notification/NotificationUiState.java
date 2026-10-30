package com.ht.robotvacuumapp.viewmodel.notification;

import com.ht.robotvacuumapp.api.dto.AlertResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NotificationUiState {
    public final List<AlertResponse> alerts;
    public final boolean isLoading, isLoadingMore, loaded, hasMore, requiresLogin;
    public final String listError, loadMoreError, sessionMessage;
    public final Long selectedId;
    public final String selectedTitle;
    public final AlertResponse detail;
    public final long detailToken;
    public final boolean detailLoading, isMarkingRead, readAttempted;
    public final String detailError, markError;

    private NotificationUiState(Builder b) {
        alerts = Collections.unmodifiableList(new ArrayList<>(b.alerts));
        isLoading = b.isLoading;
        isLoadingMore = b.isLoadingMore;
        loaded = b.loaded;
        hasMore = b.hasMore;
        requiresLogin = b.requiresLogin;
        listError = b.listError;
        loadMoreError = b.loadMoreError;
        sessionMessage = b.sessionMessage;
        selectedId = b.selectedId;
        selectedTitle = b.selectedTitle;
        detail = b.detail;
        detailToken = b.detailToken;
        detailLoading = b.detailLoading;
        isMarkingRead = b.isMarkingRead;
        readAttempted = b.readAttempted;
        detailError = b.detailError;
        markError = b.markError;
    }

    Builder toBuilder() { return new Builder(this); }
    static final class Builder {
        List<AlertResponse> alerts = Collections.emptyList();
        boolean isLoading, isLoadingMore, loaded, hasMore, requiresLogin;
        String listError, loadMoreError, sessionMessage;
        Long selectedId;
        String selectedTitle;
        AlertResponse detail;
        long detailToken;
        boolean detailLoading, isMarkingRead, readAttempted;
        String detailError, markError;
        Builder() { }
        Builder(NotificationUiState s) {
            alerts = s.alerts;
            isLoading = s.isLoading;
            isLoadingMore = s.isLoadingMore;
            loaded = s.loaded;
            hasMore = s.hasMore;
            requiresLogin = s.requiresLogin;
            listError = s.listError;
            loadMoreError = s.loadMoreError;
            sessionMessage = s.sessionMessage;
            selectedId = s.selectedId;
            selectedTitle = s.selectedTitle;
            detail = s.detail;
            detailToken = s.detailToken;
            detailLoading = s.detailLoading;
            isMarkingRead = s.isMarkingRead;
            readAttempted = s.readAttempted;
            detailError = s.detailError;
            markError = s.markError;
        }
        NotificationUiState build() { return new NotificationUiState(this); }
    }
}
