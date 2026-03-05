package com.example.MAD;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AppHomePage extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_home_page);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("userEmail");
        String userName = intent.getStringExtra("userName");
        String dob = intent.getStringExtra("dob");
        String workingStatus = intent.getStringExtra("workingStatus");
        String sector = intent.getStringExtra("sector");

        //++++++++++++ LINKED MAIL DE +++++++++++++++++ Initialize UserSessionManager
        UserSessionManager.getInstance().setUserEmail(userEmail);
        UserSessionManager.getInstance().setUserName(userName);
        UserSessionManager.getInstance().setDob(dob);
        UserSessionManager.getInstance().setWorkingStatus(workingStatus);
        UserSessionManager.getInstance().setSector(sector);


        // Prepare the Bundle
        Bundle args = new Bundle();
        args.putString("userEmail", userEmail);
        args.putString("userName", userName);
        args.putString("dob", dob);
        args.putString("workingStatus", workingStatus);
        args.putString("sector", sector);

        //Solved the click here click there problem
        bottomNav = findViewById(R.id.bottom_nav);
        navController = Navigation.findNavController(this, R.id.fragment_container);

        navController.setGraph(navController.getGraph(), args);

        // Link BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Make sure menu items match destination IDs
        bottomNav.getMenu().findItem(R.id.careerFragment).setChecked(true);

        // Setup the NavigationUI
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Handle bottom navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Check which item was clicked and navigate accordingly
            if (itemId == R.id.homeFragment) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.searchFragment) {
                navController.navigate(R.id.searchFragment);
                return true;
            } else if (itemId == R.id.careerFragment) {
                navController.navigate(R.id.careerFragment);
                return true;
            }else if (itemId == R.id.profileFragment) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Clear all selections first
            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                bottomNav.getMenu().getItem(i).setChecked(false);
            }

            // Check the appropriate bottom navigation item based on the current destination
            if (destination.getId() == R.id.homeFragment) {
                bottomNav.getMenu().findItem(R.id.homeFragment).setChecked(true);
            } else if (destination.getId() == R.id.careerFragment ||
                    destination.getId() == R.id.mentorshipFragment ||
                    destination.getId() == R.id.articleFragment ||
                    destination.getId() == R.id.forumFragment ||
                    destination.getId() == R.id.healthFragment ||
                    destination.getId() == R.id.articleDetailFragment ||
                    destination.getId() == R.id.scheduleFragment ||
                    destination.getId() == R.id.calendarFragment) {
                bottomNav.getMenu().findItem(R.id.careerFragment).setChecked(true);
            } else if(destination.getId()==R.id.searchFragment||
                    destination.getId() == R.id.courseFragment||
                    destination.getId() == R.id.courseDetailsFragment||
                    destination.getId() == R.id.jobSearchFragment||
                    destination.getId() == R.id.jobDetailsFragment||
                    destination.getId() == R.id.notiRequestFragment||
                    destination.getId() == R.id.notificationStatus||
                    destination.getId() == R.id.notificationRequest||
                    destination.getId() == R.id.partnershipProgramFragment||
                    destination.getId() == R.id.eventsFragment||
                    destination.getId() == R.id.createNewJobFragment||
                    destination.getId() == R.id.jobMainFragment||
                    destination.getId() == R.id.jobFilterFragment||
                    destination.getId() == R.id.mapsFragment||
                    destination.getId() == R.id.savedFragment||
                    destination.getId() == R.id.jobPostedFragment||
                    destination.getId() == R.id.courseDetailsFragment){
                    bottomNav.getMenu().findItem(R.id.searchFragment).setChecked(true);
            } else if(destination.getId()==R.id.profileFragment||
                    destination.getId() == R.id.selfCompanyFragment||
                    destination.getId() == R.id.selfSeekerFragment||
                    destination.getId() == R.id.companyEditFragment||
                    destination.getId() == R.id.seekerEditFragment||
                    destination.getId() == R.id.seekerSettingFragment||
                    destination.getId() == R.id.profileSeekerViewRate||
                    destination.getId() == R.id.companyViewRateFragment){
                bottomNav.getMenu().findItem(R.id.profileFragment).setChecked(true);
            }

            else {
                // For other destinations, check the corresponding menu item
                MenuItem item = bottomNav.getMenu().findItem(destination.getId());
                if (item != null) {
                    item.setChecked(true);
                }
            }
        });

        // Add Main Fragments with Arguments
        setupMainFragments(args);
    }

    private void setupMainFragments(Bundle args) {
        // Create instances of all main fragments
        AppHome_Fragment homeFragment = new AppHome_Fragment();
        homeFragment.setArguments(args);

        Search_Fragment searchFragment = new Search_Fragment();
        searchFragment.setArguments(args);

        CareerMain_Fragment careerFragment = new CareerMain_Fragment();
        careerFragment.setArguments(args);

        ProfileIdentifyFragment profileFragment = new ProfileIdentifyFragment();
        profileFragment.setArguments(args);

        // Replace the initial fragment (e.g., AppHome_Fragment)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (!navController.popBackStack()) {
            // If we can't pop the back stack, handle it here
            if (navController.getCurrentDestination().getId() != R.id.homeFragment) {
                // If we're not on the home fragment, navigate to it
                navController.navigate(R.id.homeFragment);
            } else {
                super.onBackPressed();
            }
        }
    }
}