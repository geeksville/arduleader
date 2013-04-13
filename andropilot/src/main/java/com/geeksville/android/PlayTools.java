package com.geeksville.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Work around a rare autobug: Some devices have old versions of google play
 * https://code.google.com/p/android/issues/detail?id=42543
 * 
 */
public class PlayTools {

	public static boolean checkForServices(final Activity context) {
		// See if google play services are installed.
		boolean services = false;
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo("com.google.android.gms", 0);
			services = true;
		} catch (PackageManager.NameNotFoundException e) {
			services = false;
		}

		if (services) {
			// Ok, do whatever.
			return true;
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			// set dialog message
			alertDialogBuilder
					.setTitle("Google Play Services")
					.setMessage(
							"The map requires Google Play Services to be installed.")
					.setCancelable(true)
					.setPositiveButton("Install",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									// Try the new HTTP method (I assume that is
									// the official way now given that google
									// uses it).
									try {
										Intent intent = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
										intent.setPackage("com.android.vending");
										context.startActivity(intent);
									} catch (ActivityNotFoundException e) {
										// Ok that didn't work, try the market
										// method.
										try {
											Intent intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse("market://details?id=com.google.android.gms"));
											intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
											intent.setPackage("com.android.vending");
											context.startActivity(intent);
										} catch (ActivityNotFoundException f) {
											// Ok, weird. Maybe they don't have
											// any market app. Just show the
											// website.

											Intent intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
											intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
											context.startActivity(intent);
										}
									}
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).create().show();

			return false;
		}
	}
}
