package com.example.MAD;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private LatLng latLng; // To store current LatLng
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 99;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLastLocation();

        Button btnConfirm = rootView.findViewById(R.id.btnConfirmLoc);
        btnConfirm.setOnClickListener(v -> {
            if (latLng != null) {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Location", requireActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("LATITUDE", (float) latLng.latitude);
                editor.putFloat("LONGITUDE", (float) latLng.longitude);
                editor.putInt("RADIUS", 100); // Add default radius when setting location
                editor.apply();

                Toast.makeText(requireContext(), "Location Saved", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.jobFilterFragment);
            } else {
                Toast.makeText(requireContext(), "Location not available. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                latLng = new LatLng(location.getLatitude(), location.getLongitude()); // Set LatLng
                Toast.makeText(requireContext(),
                        "Current Location: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
                loadMap();
            } else {
                Toast.makeText(requireContext(), "Failed to get location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (latLng != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("My Current Location");
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else {
            Toast.makeText(requireContext(), "Location not set. Try again.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(requireContext(), "Permission denied. Unable to fetch location.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
