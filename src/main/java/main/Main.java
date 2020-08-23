package main;

import com.sun.javafx.application.LauncherImpl;

import application.ApplicationPreloader;
import application.AutoWebApplication;

public class Main {
	public static void main(String[] args) {
		LauncherImpl.launchApplication(AutoWebApplication.class, ApplicationPreloader.class, args);
	}
}
