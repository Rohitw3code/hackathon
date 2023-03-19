package com.lapperapp.laper.ui.home;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ru.nikartm.support.ImageBadgeView;

public class NotificationCounter {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference userRef = db.collection("users");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final ImageBadgeView imageBadgeView;

    public NotificationCounter(ImageBadgeView imageBadgeView) {
        this.imageBadgeView = imageBadgeView;
    }

    public void fetchNotificationCount() {
        userRef.document(auth.getUid())
                .get().addOnSuccessListener(doc -> {
                    long count = doc.getLong("notificationCount");
                    imageBadgeView.setShowCounter(true);
                    imageBadgeView.setBadgeValue((int) count);
                });
    }


}
