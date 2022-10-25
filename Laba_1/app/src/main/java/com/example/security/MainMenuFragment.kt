package com.example.security

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainMenuFragment : Fragment(R.layout.main_menu) {
    private lateinit var navController: NavController
    private lateinit var btnAuth: Button
    private lateinit var btnData: Button
    private lateinit var btnSignOutFragment: Button
    private lateinit var btnAvatarName: Button
    private lateinit var btnChangeEmail: Button
    private lateinit var btnZone: Button
    private lateinit var btnDelete: Button

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view);

        btnData = view.findViewById(R.id.btn_data)
        btnSignOutFragment = view.findViewById(R.id.btn_sign_out_fragment)
        btnAvatarName = view.findViewById(R.id.btn_avatar_name)
        btnChangeEmail = view.findViewById(R.id.btn_email)
        btnZone = view.findViewById(R.id.btn_zone)
        btnDelete = view.findViewById(R.id.btn_delete)

        auth = Firebase.auth

        btnData.setOnClickListener {
            navController.navigate(R.id.action_mainMenu_to_personalData)
        }
        btnSignOutFragment.setOnClickListener {
            navController.navigate(R.id.action_mainMenu_to_signOutFragment)
        }
        btnAvatarName.setOnClickListener {
            navController.navigate(R.id.action_mainMenu_to_changePersonalDataFragment)
        }
        btnChangeEmail.setOnClickListener {
            navController.navigate(R.id.action_mainMenu_to_changeEmail)
        }
        btnZone.setOnClickListener {
            val user = Firebase.auth.currentUser!!
            navController.navigate(R.id.action_mainMenu_to_zone)
        }
        btnDelete.setOnClickListener {
            navController.navigate(R.id.action_mainMenu_to_deleteAccountFragment)
        }
    }
}