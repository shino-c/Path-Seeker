package com.example.MAD;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private List<Event> eventsList;
    private EventActionListener listener;

    public interface EventActionListener {
        void onAddToCalendar(Event event);
        void onNavigate(Event event);
    }

    public EventsAdapter(List<Event> eventsList, EventActionListener listener) {
        this.eventsList = eventsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.time.setText(event.getStartTime() + " - " + event.getEndTime());
        holder.venue.setText(event.getVenue());

        holder.markCalendar.setOnClickListener(v -> listener.onAddToCalendar(event));
        holder.navigate.setOnClickListener(v -> listener.onNavigate(event));
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, venue;
        ImageButton markCalendar, navigate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            time = itemView.findViewById(R.id.event_time);
            venue = itemView.findViewById(R.id.event_venue);
            markCalendar = itemView.findViewById(R.id.btn_mark_calendar);
            navigate = itemView.findViewById(R.id.btn_navigate);
        }
    }
}
