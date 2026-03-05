package com.example.MAD;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsFragment extends Fragment implements OnMapReadyCallback {
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "EventsFragment";
    private MapView mapView;
    private GoogleMap googleMap;
    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;
    private final List<Event> eventsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        // Initialize MapView
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize RecyclerView
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setHasFixedSize(true);

        // Initialize EventsAdapter and set event listeners
        initializeLocalEvents();
        eventsAdapter = new EventsAdapter(eventsList, new EventsAdapter.EventActionListener() {
            @Override
            public void onAddToCalendar(Event event) {
                addToGoogleCalendar(event);
            }

            @Override
            public void onNavigate(Event event) {
                navigateToLocation(event);
            }
        });
        eventsRecyclerView.setAdapter(eventsAdapter);

        ImageView backIcon = view.findViewById(R.id.back_button);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void initializeLocalEvents() {
        Log.e(TAG, "Initializing local events...");
        eventsList.add(new Event("1", "AI Open Day", "17/01/2025", "08:00 AM", "05:00 PM", "FCSIT, UM", 3.1209, 101.6538));
        eventsList.add(new Event("2", "AWS Community Day", "01/02/2025", "09:00 AM", "04:00 PM", "KLCC", 3.1579, 101.7114));
        eventsList.add(new Event("3", "DevFest 2025", "22/02/2025", "09:00 AM", "06:00 PM", "KLCC", 3.1579, 101.7114));
        Log.e(TAG, "Local events initialized: " + eventsList.size());
    }

    private void addToGoogleCalendar(Event event) {
        Log.e(TAG, "Adding event to Google Calendar: " + event.getTitle());

        // Check for calendar permissions first
        if (getContext() == null ||
                (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED)) {

            requestPermissions(
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                    CALENDAR_PERMISSION_REQUEST_CODE
            );
            return;
        }

        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        try {
            // Parse date (format: DD/MM/YYYY)
            String[] dateParts = event.getDate().split("/");
            int year = Integer.parseInt(dateParts[2]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Months are zero-based
            int day = Integer.parseInt(dateParts[0]);

            // Parse start time (format: HH:MM AM/PM)
            String[] startTimeParts = event.getStartTime().split(" ");
            String[] startHourMin = startTimeParts[0].split(":");
            int startHour = Integer.parseInt(startHourMin[0]) % 12;
            if (startTimeParts[1].equalsIgnoreCase("PM")) startHour += 12;
            int startMinute = Integer.parseInt(startHourMin[1]);

            // Parse end time (format: HH:MM AM/PM)
            String[] endTimeParts = event.getEndTime().split(" ");
            String[] endHourMin = endTimeParts[0].split(":");
            int endHour = Integer.parseInt(endHourMin[0]) % 12;
            if (endTimeParts[1].equalsIgnoreCase("PM")) endHour += 12;
            int endMinute = Integer.parseInt(endHourMin[1]);

            beginTime.set(year, month, day, startHour, startMinute);
            endTime.set(year, month, day, endHour, endMinute);

            // Try direct calendar content provider insertion as backup method
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, 1); // Default calendar
            values.put(CalendarContract.Events.TITLE, event.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION, "Event at " + event.getVenue());
            values.put(CalendarContract.Events.EVENT_LOCATION, event.getVenue());
            values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());

            Uri uri = getContext().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                Toast.makeText(getContext(), "Event added to calendar", Toast.LENGTH_SHORT).show();
                return;
            }

            // If content provider insertion fails, try intent method as fallback
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getVenue())
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Event at " + event.getVenue());

            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No calendar app found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding event to calendar: " + e.getMessage());
            Toast.makeText(getContext(), "Error adding event to calendar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry adding the event
                // You'll need to store the event temporarily or pass it back somehow
                Toast.makeText(getContext(), "Calendar permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Calendar permission required to add events", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void navigateToLocation(Event event) {
        Log.e(TAG, "Navigating to location: " + event.getVenue());
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + event.getLatitude() + "," + event.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.e(TAG, "No app available to navigate to location.");
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        Log.e(TAG, "Map is ready");
        for (Event event : eventsList) {
            LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(event.getTitle())
                    .snippet(event.getVenue())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
        LatLng defaultLocation = new LatLng(3.1209, 101.6538);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
    }

    // MapView lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
