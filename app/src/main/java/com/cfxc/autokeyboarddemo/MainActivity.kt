package com.cfxc.autokeyboarddemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        navController = (nav_host_fragment as NavHostFragment).navController
        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                drawer_layout
        )

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination is FragmentNavigator.Destination) {
                Log.e("Navigating to fragment:", destination.className)

                // build the title from the label and arguments
                // just like AbstractAppBarOnDestinationChangedListener
                val label = destination.getLabel()
                if (label != null) {
                    // Fill in the data pattern with the args to build a valid URI
                    val title = StringBuffer()
                    val fillInPattern = Pattern.compile("\\{(.+?)\\}")
                    val matcher = fillInPattern.matcher(label)
                    while (matcher.find()) {
                        val argName = matcher.group(1)
                        if (arguments != null && arguments.containsKey(argName)) {
                            matcher.appendReplacement(title, "")
                            title.append(arguments.get(argName).toString())
                        } else {
                            throw IllegalArgumentException(
                                    "Could not find " + argName + " in "
                                            + arguments + " to fill label " + label
                            )
                        }
                    }
                    matcher.appendTail(title)
                    updateActionBarTitle(title.toString())
                    return@addOnDestinationChangedListener
                }

                //dynamic use argument key GDConstants.INTENT_EXTRA_CLIENT_TITLE to set title
                val title = arguments?.get("intent_extra_client_title") as? String
                if (!title.isNullOrBlank()) {
                    updateActionBarTitle(title)
                }
            }
        }
    }

    fun updateActionBarTitle(title: String) {
        toolbar.title = title
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawer_layout)
    }
}