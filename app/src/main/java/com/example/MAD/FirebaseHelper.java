package com.example.MAD;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private DatabaseReference mentorsRef;
    private DatabaseReference bookedSlotsRef;
    private DatabaseReference healthExpertsRef;
    private DatabaseReference databaseReference;
    private DatabaseReference articlesRef;

    public interface DataCallback {
        void onSuccess(List<Mentor> mentors);
        void onFailure(Exception e);
    }

    public interface ArticleCallback {
        void onSuccess(List<Article> articles);
        void onFailure(Exception e);
    }

    public FirebaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pathseeker-40c02-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = database.getReference("mentors");  // Keep original reference
        mentorsRef = database.getReference("mentors");
        bookedSlotsRef = database.getReference("bookedSlots");
        healthExpertsRef = database.getReference("health experts");
        articlesRef = database.getReference("articles");
    }

    private ValueEventListener healthExpertsListener;

    public void fetchHealthExperts(DataCallback callback) {
        healthExpertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("FirebaseHelper", "onDataChange triggered: " + snapshot.toString());

                if (snapshot.exists()) {
                    List<Mentor> experts = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Mentor expert = child.getValue(Mentor.class);
                        Log.d("FirebaseHelper", "Mentor: " + expert); // Log the fetched mentor
                        if (expert != null) {
                            experts.add(expert);
                        }
                    }
                    callback.onSuccess(experts);
                } else {
                    Log.d("FirebaseHelper", "Snapshot is empty");
                    callback.onSuccess(new ArrayList<>()); // Send empty list to avoid null pointers
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseHelper", "DatabaseError: " + error.getMessage());
                callback.onFailure(error.toException());
            }
        });
    }


    public void removeHealthExpertsListener() {
        if (healthExpertsListener != null) {
            healthExpertsRef.removeEventListener(healthExpertsListener);
            healthExpertsListener = null;
        }
    }

    public void fetchMentors(DataCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("FirebaseHelper", "DataSnapshot exists: " + snapshot.getChildrenCount());
                } else {
                    Log.d("FirebaseHelper", "DataSnapshot does not exist");
                }

                List<Mentor> mentors = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Mentor mentor = child.getValue(Mentor.class);
                    if (mentor != null) {
                        mentors.add(mentor);
                    } else {
                        Log.e("FirebaseHelper", "Mentor is null for child: " + child.getKey());
                    }
                }

                if (!mentors.isEmpty()) {
                    Log.d("FirebaseHelper", "Mentors fetched: " + mentors.size());
                } else {
                    Log.e("FirebaseHelper", "No mentors fetched");
                }
                callback.onSuccess(mentors);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseHelper", "Error fetching mentors: " + error.getMessage());
                callback.onFailure(error.toException());
            }
        });
    }

    public void fetchArticle(ArticleCallback callback) {
        articlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Article> articles = new ArrayList<>();

                try {
                    for (DataSnapshot articleSnapshot : snapshot.getChildren()) {
                        // Skip null entries in the database
                        if (articleSnapshot.getValue() == null) {
                            continue;
                        }

                        Article article = new Article();

                        // Map the database fields to Article object fields
                        DataSnapshot titleSnap = articleSnapshot.child("article_title");
                        DataSnapshot subtitleSnap = articleSnapshot.child("article_subtitle");
                        DataSnapshot authorSnap = articleSnapshot.child("article_author");
                        DataSnapshot imageSnap = articleSnapshot.child("article_pic");
                        DataSnapshot positionSnap = articleSnapshot.child("article_position");
                        DataSnapshot contentSnap = articleSnapshot.child("article_content");

                        // Only add article if it has the required fields
                        if (titleSnap.exists() && subtitleSnap.exists()) {
                            article.title = titleSnap.getValue(String.class);
                            article.subtitle = subtitleSnap.getValue(String.class);
                            article.author = authorSnap.getValue(String.class);
                            article.image = imageSnap.getValue(String.class);
                            article.position = positionSnap.getValue(String.class);
                            article.content = contentSnap.getValue(String.class);

                            articles.add(article);
                        }
                    }

                    Log.d("FirebaseHelper", "Successfully fetched " + articles.size() + " articles");
                    callback.onSuccess(articles);

                } catch (Exception e) {
                    Log.e("FirebaseHelper", "Error parsing article data: " + e.getMessage());
                    callback.onFailure(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseHelper", "Database error: " + error.getMessage());
                callback.onFailure(error.toException());
            }
        });
    }

    private ValueEventListener articlesListener;

    public void removeArticleListener() {
        if (articlesListener != null) {
            articlesRef.removeEventListener(articlesListener);
            articlesListener = null;
        }
    }

    private String getCurrentUserEmail() {
        return UserSessionManager.getInstance().getUserEmail();
    }

    // Update the bookSlot method to use getCurrentUserEmail()
    public void bookSlot(String mentorId, String date, String timeSlot, String userEmail, DataCallback callback) {
        Log.d("FirebaseHelper", "Starting bookSlot with mentorId: " + mentorId + ", date: " + date + ", timeSlot: " + timeSlot + ", userEmail: " + userEmail);

        final String finalUserEmail;
        if (userEmail == null || userEmail.isEmpty()) {
            finalUserEmail = UserSessionManager.getInstance().getUserEmail();
            Log.d("FirebaseHelper", "Got email from UserSessionManager: " + finalUserEmail);
            if (finalUserEmail == null) {
                callback.onFailure(new Exception("User not logged in"));
                return;
            }
        } else {
            finalUserEmail = userEmail;
        }

        // Parse the date
        String[] dateParts = date.split("/");
        String day = dateParts[0].trim();
        String month = dateParts[1].trim();
        String year = dateParts[2].trim();

        Log.d("FirebaseHelper", "Parsed date - day: " + day + ", month: " + month + ", year: " + year);

        // First check all bookings for the same date and time slot
        bookedSlotsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FirebaseHelper", "Failed to check existing bookings", task.getException());
                callback.onFailure(task.getException());
                return;
            }

            DataSnapshot allBookings = task.getResult();
            boolean hasConflict = false;
            String conflictReason = "";

            // Check all mentors
            for (DataSnapshot mentorSnapshot : allBookings.getChildren()) {
                DataSnapshot yearSnap = mentorSnapshot.child(year);
                if (!yearSnap.exists()) continue;

                DataSnapshot monthSnap = yearSnap.child(month);
                if (!monthSnap.exists()) continue;

                DataSnapshot daySnap = monthSnap.child(day);
                if (!daySnap.exists()) continue;

                // Case 1: Check if user already has a booking with this mentor on this day
                if (mentorSnapshot.getKey().equals(mentorId)) {
                    for (DataSnapshot timeSlotSnap : daySnap.getChildren()) {
                        try {
                            BookingData bookingData = timeSlotSnap.getValue(BookingData.class);
                            if (bookingData != null &&
                                    bookingData.isBooked() &&
                                    bookingData.getUserEmail().equals(finalUserEmail)) {
                                hasConflict = true;
                                conflictReason = "You already have a booking with this mentor on the selected date";
                                break;
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseHelper", "Error reading booking data", e);
                        }
                    }
                }

                // Case 2: Check if user has any booking at this time slot with any mentor
                DataSnapshot timeSlotSnap = daySnap.child(timeSlot);
                if (timeSlotSnap.exists()) {
                    try {
                        BookingData bookingData = timeSlotSnap.getValue(BookingData.class);
                        if (bookingData != null &&
                                bookingData.isBooked() &&
                                bookingData.getUserEmail().equals(finalUserEmail)) {
                            hasConflict = true;
                            conflictReason = "You already have a booking with another mentor at this time";
                            break;
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseHelper", "Error reading booking data", e);
                    }
                }
            }

            if (hasConflict) {
                callback.onFailure(new Exception(conflictReason));
                return;
            }

            DatabaseReference bookingRef = bookedSlotsRef
                    .child(mentorId)
                    .child(year)
                    .child(month)
                    .child(day)
                    .child(timeSlot);

            Log.d("FirebaseHelper", "Writing to Firebase path: " + bookingRef.toString());

            BookingData bookingData = new BookingData(finalUserEmail, true);
            Log.d("FirebaseHelper", "Booking data: " + bookingData.getUserEmail() + ", " + bookingData.isBooked());

            bookingRef.setValue(bookingData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseHelper", "Successfully wrote booking to Firebase");
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Failed to write booking to Firebase", e);
                        callback.onFailure(e);
                    });
        });
    }

    public void isSlotBooked(String mentorId, String date, String timeSlot, DataCallback callback) {
        try {
            if (mentorId == null || date == null || timeSlot == null || date.trim().isEmpty()) {
                callback.onFailure(new IllegalArgumentException("Invalid parameters"));
                return;
            }

            String[] dateParts = date.split("/");
            if (dateParts.length != 3) {
                callback.onFailure(new IllegalArgumentException("Invalid date format"));
                return;
            }

            String day = dateParts[0].trim();
            String month = dateParts[1].trim();
            String year = dateParts[2].trim();

            DatabaseReference bookingRef = bookedSlotsRef
                    .child(mentorId)
                    .child(year)
                    .child(month)
                    .child(day)
                    .child(timeSlot);

            bookingRef.get().addOnSuccessListener(snapshot -> {
                List<Mentor> result = new ArrayList<>();
                if (snapshot.exists()) {
                    try {
                        Object value = snapshot.getValue();
                        boolean isBooked = false;

                        if (value instanceof Boolean) {
                            // Old format
                            isBooked = (Boolean) value;
                        } else {
                            // New format
                            BookingData bookingData = snapshot.getValue(BookingData.class);
                            isBooked = bookingData != null && bookingData.isBooked();
                        }

                        if (isBooked) {
                            result.add(new Mentor());
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseHelper", "Error reading booking data", e);
                    }
                }
                callback.onSuccess(result);
            }).addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public static class BookingData {
        private String userEmail;
        private boolean booked;

        public BookingData() {
            // Default constructor required for calls to DataSnapshot.getValue(BookingData.class)
        }

        public BookingData(String userEmail, boolean booked) {
            this.userEmail = userEmail;
            this.booked = booked;
        }

        public String getUserEmail() { return userEmail; }
        public boolean isBooked() { return booked; }
    }
}